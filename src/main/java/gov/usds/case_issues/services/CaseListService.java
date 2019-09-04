package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.NoteSummary;

/**
 * Service object for fetching paged lists of cases (and information about case counts)
 * for the main hit-list API.
 */
@Service
@Transactional(readOnly=true)
public class CaseListService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseListService.class);

	@Autowired
	private CaseTypeRepository _caseTypeRepo;
	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;
	@Autowired
	private BulkCaseRepository _bulkRepo;

	@Autowired
	private CaseIssueRepository _issueRepo;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private CaseAttachmentService _attachmentService;

	public List<TroubleCase> getCases(String caseManagementSystemTag, String caseTypeTag, String query) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		LOG.debug("Request for query cases by: {}", query);

		if (query == null) {
			return new ArrayList<>();
		}

		return _caseRepo.getFirst5ByCaseManagementSystemAndCaseTypeAndReceiptNumberContains(
			translated.getCaseManagementSystem(),
			translated.getCaseType(),
			query
		);
	}

	public List<CaseSummary> getActiveCases(String caseManagementSystemTag, String caseTypeTag, Pageable pageRequest) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		LOG.debug("Paged request for active cases: {} {}", pageRequest.getPageSize(), pageRequest.getPageNumber());
		Page<Object[]> cases = _bulkRepo.getActiveCases(
			translated.getCaseManagementSystemId(), translated.getCaseTypeId(), pageRequest);
		return rewrap(cases.getContent(), false);
	}

	public List<CaseSummary> getSnoozedCases(String caseManagementSystemTag, String caseTypeTag, Pageable pageRequest) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		LOG.debug("Paged request for snoozed cases: {} {}", pageRequest.getPageSize(), pageRequest.getPageNumber());
		Page<Object[]> cases = _bulkRepo.getSnoozedCases(
			translated.getCaseManagementSystemId(), translated.getCaseTypeId(), pageRequest);
		return rewrap(cases.getContent(), true);
	}

	public Map<String, Number> getSummaryInfo(String caseManagementSystemTag, String caseTypeTag) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		return _bulkRepo.getSnoozeSummary(translated.getCaseManagementSystemId(), translated.getCaseTypeId())
				.stream()
				.collect(Collectors.toMap(a->((String) a[0]).trim(), a->(Number) a[1]));
	}
	
	public CaseGroupInfo translatePath(String caseManagementSystemTag, String caseTypeTag) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByCaseTypeTag(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		return new CaseGroupInfo(caseManagementSystem, caseType);
	}

	/**
	 * Hard set the list of open issues for the given {@link CaseManagementSystem}, {@link CaseType} and
	 * {@link CaseIssue#getIssueType()} to the supplied list of cases.
	 * <ul>
	 * <li>Issues that are open for cases that are not on the list will be marked closed;</li>
 	 * <li>Cases that are in the list and do not exist will be created and have an
 	 *     issue of the correct type created.</li>
 	 * <li>Cases that are in the list and already exist will have their additional data updated,
 	 * 		and an issue created if no open issue of the correct type exists;</li>
 	 * </ul>  
	 * @param systemTag the {@link CaseManagementSystem#getCaseManagementSystemTag()} for the system we are updating.
	 * @param caseTypeTag {@link CaseType#getCaseTypeTag()} for the case type we are updating.
	 * @param issueTypeTag the type of issue we are updating.
	 * @param newIssueCases case information for each case that has this issue as of the date for which we are uploading data.
	 * @param eventDate the date (usually but not always {@link ZonedDateTime#now()}) to attach to this update
	 *    (this will be reflected in the {@link CaseIssue#getIssueCreated()} and {@link CaseIssue#getIssueClosed()}
	 *    values that are set by this method).
	 * @throws ApiModelNotFoundException if the {@link CaseManagementSystem} or {@link CaseType} could not be found.
	 */
	@Transactional(readOnly=false)
	@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).UPDATE_ISSUES.name())")
	public void putIssueList(String systemTag, String caseTypeTag, String issueTypeTag,
			Iterable<CaseRequest> newIssueCases, ZonedDateTime eventDate) {
		CaseGroupInfo translated = translatePath(systemTag, caseTypeTag);
		List<CaseIssue> currentIssues = _issueRepo.findActiveIssues(translated.getCaseManagementSystem(), translated.getCaseType(),
				issueTypeTag);
		Map<String, CaseIssue> currentMap = currentIssues.stream().collect(Collectors.toMap(i->i.getIssueCase().getReceiptNumber(), i->i));

		// build a list containing only CaseSummary objects with no existing CaseIssue
		List<CaseRequest> requestedNewIssues = new ArrayList<>();
		int updatedCaseCount = 0;
		for (CaseRequest caseSummary : newIssueCases) {
			CaseIssue existingIssue = currentMap.remove(caseSummary.getReceiptNumber());
			if (null == existingIssue) {
				requestedNewIssues.add(caseSummary);
			} else {
				updatedCaseCount++;
				TroubleCase issueCase = existingIssue.getIssueCase();
				issueCase.getExtraData().putAll(caseSummary.getExtraData());
				if (issueCase.getCaseType() != translated.getCaseType()) {
					LOG.error("Illegal update of case type requested for {}", issueCase.getReceiptNumber());
				}
			}
		}

		LOG.debug("For PUT of {}/{}/{}, opening {} and closing {} issues; updating cases of {} existing issues", systemTag, caseTypeTag, issueTypeTag, 
				requestedNewIssues.size(), currentMap.size(), updatedCaseCount);
		// terminate all the remaining issues in the current collection
		// this could also be done directly in the database, which might not be a bad idea?
		currentMap.values().forEach(i -> i.setIssueClosed(eventDate));

		// get or create cases
		HashSet<String> newReceipts = requestedNewIssues.stream().map(CaseRequest::getReceiptNumber)
				.collect(Collectors.toCollection(HashSet::new));
		Map<String, TroubleCase> existingCases = _caseRepo.getAllByCaseManagementSystemAndReceiptNumberIn(
				translated.getCaseManagementSystem(), newReceipts).stream().collect(Collectors.toMap(TroubleCase::getReceiptNumber, i->i));
		newReceipts.removeAll(existingCases.keySet());

		LOG.debug("For PUT of {}/{}/{}, found {} existing cases, creating {}", systemTag, caseTypeTag, issueTypeTag,
				existingCases.size(), newReceipts.size());
		
		List<TroubleCase> unsavedCases = new ArrayList<>();
		// create or update cases
		for (CaseRequest candidate : requestedNewIssues) {
			String receiptNumber = candidate.getReceiptNumber();
			if (newReceipts.contains(receiptNumber)) {
				unsavedCases.add(new TroubleCase(translated.getCaseManagementSystem(),
						receiptNumber, translated.getCaseType(),
						candidate.getCaseCreation(), candidate.getExtraData()));
			} else {
				TroubleCase found = existingCases.get(receiptNumber);
				if (found.getCaseType() != translated.getCaseType()) {
					LOG.error("Illegal update of case type requested for {}", receiptNumber);
				}
				found.getExtraData().putAll(candidate.getExtraData());
			}
		}
		// slightly dumb bit of DRY factory work
		Function<? super TroubleCase, ? extends CaseIssue> createIssue = tc -> new CaseIssue(tc, issueTypeTag, eventDate);
		
		// well this is ugly
		Iterable<TroubleCase> newlySavedCases = _caseRepo.saveAll(unsavedCases);
		List<CaseIssue> newIssues = new ArrayList<>();
		newlySavedCases.forEach(tc -> newIssues.add(createIssue.apply(tc)));

		LOG.debug("For PUT of {}/{}/{}, attempting to save {} new issues", systemTag, caseTypeTag, issueTypeTag,
				newIssues.size() + existingCases.size());

		// aaaand save everything!
		_issueRepo.saveAll(
			Stream.concat(
				existingCases.values().stream().map(createIssue),
				newIssues.stream()
			).collect(Collectors.toSet())
		);
	}

	private List<CaseSummary> rewrap(List<Object[]> queryResult, boolean includeNotes) {
		Function<? super Object[], ? extends CaseSummary> mapper = row ->{
			TroubleCase rootCase = (TroubleCase) row[0];
			ZonedDateTime lastSnoozeEnd = (ZonedDateTime) row[1];
			CaseSnoozeSummary summary = lastSnoozeEnd == null ? null : _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(rootCase).get();
			List<NoteSummary> notes = null;
			if(includeNotes) {
				notes = _attachmentService.findNotesForCase(rootCase).stream().map(NoteSummary::new).collect(Collectors.toList());
			}
			return new CaseSummary(rootCase, summary, notes);
		};
		return queryResult.stream().map(mapper).collect(Collectors.toList());
	}

	public static class CaseGroupInfo {

		private CaseManagementSystem _system;
		private CaseType _type;

		public CaseGroupInfo(CaseManagementSystem _system, CaseType _type) {
			super();
			this._system = _system;
			this._type = _type;
		}

		public Long getCaseManagementSystemId() {
			return _system.getInternalId();
		}

		public Long getCaseTypeId() {
			return _type.getInternalId();
		}

		public CaseManagementSystem getCaseManagementSystem() {
			return _system;
		}

		public CaseType getCaseType() {
			return _type;
		}
	}
}
