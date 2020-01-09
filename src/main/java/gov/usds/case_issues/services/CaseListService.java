package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import gov.usds.case_issues.authorization.RequireUploadPermission;
import gov.usds.case_issues.config.DataFormatSpec;
import gov.usds.case_issues.config.WebConfigurationProperties;
import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseIssueUploadRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.CaseSummaryImpl;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.services.model.CaseGroupInfo;
import gov.usds.case_issues.services.model.CasePageInfo;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.validators.TagFragment;

/**
 * Service object for fetching paged lists of cases (and information about case counts)
 * for the main hit-list API.
 */
@Service
@Transactional(readOnly=true)
@Validated
public class CaseListService implements CasePagingService, PageTranslationService {

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
	@Autowired
	private UploadStatusService _uploadStatusService; // we should not have this and the repo injected in the same class!
	@Autowired
	private CaseIssueUploadRepository _uploadRepo;
	@Autowired
	private WebConfigurationProperties _webProperties;

	public List<TroubleCase> getCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			String query) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);

		if (query == null || query.isEmpty()) {
			return new ArrayList<>();
		}

		return _caseRepo.getFirst5ByCaseManagementSystemAndCaseTypeAndReceiptNumberContains(
			translated.getCaseManagementSystem(),
			translated.getCaseType(),
			query
		);
	}

	/* (non-Javadoc)
	 * @see gov.usds.case_issues.services.CasePagingService#getActiveCases(java.lang.String, java.lang.String, java.lang.String, gov.usds.case_issues.model.DateRange, int)
	 */
	public List<CaseSummary> getActiveCases(
		@TagFragment String caseManagementSystemTag,
		@TagFragment String caseTypeTag,
		@TagFragment String receiptNumber, // wrong validator!
		DateRange caseCreationRange,
		int size
	) {
		LOG.debug(
			"Request for active cases after case with systemTag: {} and receiptNumber: {}",
			caseManagementSystemTag,
			receiptNumber
		);
		CasePageInfo translated = translatePath(caseManagementSystemTag, caseTypeTag, receiptNumber);
		List<Object[]> foundCases;
		if (translated.isFirstPage()) {
			if (caseCreationRange == null) {
				foundCases = _bulkRepo.getActiveCases(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					size
				);
			} else {
				foundCases = _bulkRepo.getActiveCases(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					caseCreationRange.getStartDate(),
					caseCreationRange.getEndDate(),
					size
				);
			}
		} else {
			if (caseCreationRange == null) {
				foundCases = _bulkRepo.getActiveCasesAfter(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					translated.getCaseCreationDate(),
					translated.getCaseId(),
					size
				);
			} else {
				foundCases = _bulkRepo.getActiveCasesAfter(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					translated.getCaseCreationDate(),
					translated.getCaseId(),
					caseCreationRange.getStartDate(),
					caseCreationRange.getEndDate(),
					size
				);

			}
		}
		return rewrap(foundCases);
	}


	/* (non-Javadoc)
	 * @see gov.usds.case_issues.services.CasePagingService#getSnoozedCases(java.lang.String, java.lang.String, java.lang.String, gov.usds.case_issues.model.DateRange, java.util.Optional, int)
	 */
	public List<CaseSummary> getSnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber, // wrong validation tag!
			DateRange caseCreationRange,
			Optional<String> snoozeReason,
			int size
	) {
		LOG.debug(
			"Request for snoozed cases after case with systemTag: {} and receiptNumber: {}",
			caseManagementSystemTag,
			receiptNumber
		);
		CasePageInfo translated = translatePath(caseManagementSystemTag, caseTypeTag, receiptNumber);
		List<Object[]> foundCases;
		if (translated.isFirstPage()) {
			if (caseCreationRange == null) {
				if (snoozeReason.isPresent()) {
					foundCases = _bulkRepo.getSnoozedCases(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						snoozeReason.get(),
						size
					);
				} else {
					foundCases = _bulkRepo.getSnoozedCases(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						size
					);
				}
			} else {
				if (snoozeReason.isPresent()) {
					foundCases = _bulkRepo.getSnoozedCases(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						caseCreationRange.getStartDate(),
						caseCreationRange.getEndDate(),
						snoozeReason.get(),
						size
					);
				} else {
					foundCases = _bulkRepo.getSnoozedCases(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						caseCreationRange.getStartDate(),
						caseCreationRange.getEndDate(),
						size
					);
				}
			}
		} else {
			TroubleCase troubleCase = translated.getCase();
			Optional<CaseSnooze> lastSnooze = _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(troubleCase);
			if (!lastSnooze.isPresent()) {
				throw new IllegalArgumentException(
					"Receipt number given does not correspond to a snoozed case"
				);
			}
			ZonedDateTime lastSnoozeEnd = lastSnooze.get().getSnoozeEnd();
			// in theory, reversing this and using isBefore would work nicely, only this might produce a 1-millisecond
			// race condition in tests
			if (!lastSnoozeEnd.isAfter(ZonedDateTime.now())) {
				throw new IllegalArgumentException("Snooze was ended for this case before page request was sent.");
			}
			if (caseCreationRange == null) {
				if (snoozeReason.isPresent()) {
					foundCases = _bulkRepo.getSnoozedCasesAfter(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						lastSnoozeEnd,
						troubleCase.getCaseCreation(),
						troubleCase.getInternalId(),
						snoozeReason.get(),
						size
					);
				} else {
					foundCases = _bulkRepo.getSnoozedCasesAfter(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						lastSnoozeEnd,
						troubleCase.getCaseCreation(),
						troubleCase.getInternalId(),
						size
					);
				}
			} else {
				if (snoozeReason.isPresent()) {
					foundCases = _bulkRepo.getSnoozedCasesAfter(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						lastSnoozeEnd,
						troubleCase.getCaseCreation(),
						troubleCase.getInternalId(),
						caseCreationRange.getStartDate(),
						caseCreationRange.getEndDate(),
						snoozeReason.get(),
						size
					);
				} else {
					foundCases = _bulkRepo.getSnoozedCasesAfter(
						translated.getCaseManagementSystemId(),
						translated.getCaseTypeId(),
						lastSnoozeEnd,
						troubleCase.getCaseCreation(),
						troubleCase.getInternalId(),
						caseCreationRange.getStartDate(),
						caseCreationRange.getEndDate(),
						size
					);
				}
			}
		}
		return rewrap(foundCases);
	}


	/* (non-Javadoc)
	 * @see gov.usds.case_issues.services.CasePagingService#getPreviouslySnoozedCases(java.lang.String, java.lang.String, java.lang.String, gov.usds.case_issues.model.DateRange, int)
	 */
	public List<CaseSummary> getPreviouslySnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			int size) {
		CasePageInfo translated = translatePath(caseManagementSystemTag, caseTypeTag, receiptNumber);
		List<Object[]> foundCases;
		if (translated.isFirstPage()) {
			if (caseCreationRange == null) {
				foundCases = _bulkRepo.getPreviouslySnoozedCases(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					size
				);
			} else {
				foundCases = _bulkRepo.getPreviouslySnoozedCases(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					caseCreationRange.getStartDate(),
					caseCreationRange.getEndDate(),
					size
				);
			}
		} else {
			if (caseCreationRange == null) {
				foundCases = _bulkRepo.getPreviouslySnoozedCasesAfter(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					translated.getCaseCreationDate(),
					translated.getCaseId(),
					size
				);
			} else {
				foundCases = _bulkRepo.getPreviouslySnoozedCasesAfter(
					translated.getCaseManagementSystemId(),
					translated.getCaseTypeId(),
					translated.getCaseCreationDate(),
					translated.getCaseId(),
					caseCreationRange.getStartDate(),
					caseCreationRange.getEndDate(),
					size
				);
			}
		}
		return rewrap(foundCases);
	}

	public Map<String, Object> getSummaryInfo(@TagFragment String caseManagementSystemTag, @TagFragment String caseTypeTag) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		Map<String, Object> caseCounts = _bulkRepo.getSnoozeSummary(translated.getCaseManagementSystemId(), translated.getCaseTypeId())
				.stream()
				.collect(Collectors.toMap(a->((String) a[0]).trim(), a->(Number) a[1]));
		CaseIssueUpload lastSuccess = _uploadStatusService.getLastUpload(
			translated.getCaseManagementSystem(), translated.getCaseType(), UploadStatus.SUCCESSFUL);
		if (lastSuccess != null) {
			caseCounts.put("lastUpdated", lastSuccess.getEffectiveDate());
		}
		return caseCounts;
	}

	public CaseGroupInfo translatePath(@TagFragment String caseManagementSystemTag, @TagFragment String caseTypeTag) {
		LOG.debug("Looking up path information for {}/{}", caseManagementSystemTag, caseTypeTag);
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByExternalId(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByExternalId(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		return new CaseGroupInfo(caseManagementSystem, caseType);
	}

	public CasePageInfo translatePath(@TagFragment String caseManagementSystemTag, @TagFragment String caseTypeTag, @TagFragment String receipt) {
		CaseGroupInfo group = translatePath(caseManagementSystemTag, caseTypeTag);
		if (receipt != null) {
			Optional<TroubleCase> lastCase =_caseRepo.findByCaseManagementSystemAndReceiptNumber(
				group.getCaseManagementSystem(),
				receipt
			);
			if (lastCase.isPresent()) {
				return new CasePageInfo(group, lastCase.get());
			}
			// I would argue that if it isn't we should throw IllegalArgumentException, but that's a breaking change
		}
		return new CasePageInfo(group, null);
	}

	/**
	 * Hard set the list of open issues for the given {@link CaseManagementSystem}, {@link CaseType} and
	 * {@link CaseIssue#getIssueType()} to the supplied list of cases.
	 * <ul>
	 * <li>Issues that are open for cases that are not on the list will be marked closed;</li>
	 * <li>Cases that are in the list and do not exist will be created and have an
	 *     issue of the correct type created.</li>
	 * <li>Cases that are in the list and already exist will have their additional data updated,
	 *     and an issue created if no open issue of the correct type exists;</li>
	 * </ul>
	 * @param systemTag the {@link CaseManagementSystem#getExternalId()} for the system we are updating.
	 * @param caseTypeTag {@link CaseType#getExternalId()} for the case type we are updating.
	 * @param issueTypeTag the type of issue we are updating.
	 * @param newIssueCases case information for each case that has this issue as of the date for which we are uploading data.
	 * @param eventDate the date (usually but not always {@link ZonedDateTime#now()}) to attach to this update
	 *    (this will be reflected in the {@link CaseIssue#getIssueCreated()} and {@link CaseIssue#getIssueClosed()}
	 *    values that are set by this method).
	 * @throws ApiModelNotFoundException if the {@link CaseManagementSystem} or {@link CaseType} could not be found.
	 */

	@Transactional(readOnly=false)
	@RequireUploadPermission
	public CaseIssueUpload putIssueList(CaseIssueUpload translated, List<CaseRequest> newIssueCases) {
		// convenience aliases to local variables, for use in closures
		final ZonedDateTime eventDate = translated.getEffectiveDate();
		final String issueTypeTag = translated.getIssueType();

		List<CaseIssue> currentIssues = _issueRepo.findActiveIssues(translated.getCaseManagementSystem(), translated.getCaseType(),
				translated.getIssueType());
		Map<String, CaseIssue> currentMap = currentIssues.stream().collect(Collectors.toMap(i->i.getIssueCase().getReceiptNumber(), i->i));

		// build a list containing only CaseSummary objects with no existing CaseIssue
		List<CaseRequest> requestedNewIssues = new ArrayList<>();
		for (CaseRequest caseSummary : newIssueCases) {
			CaseIssue existingIssue = currentMap.remove(caseSummary.getReceiptNumber());
			if (null == existingIssue) {
				requestedNewIssues.add(caseSummary);
			} else {
				TroubleCase issueCase = existingIssue.getIssueCase();
				issueCase.getExtraData().putAll(caseSummary.getExtraData());
				if (issueCase.getCaseType() != translated.getCaseType()) {
					LOG.error("Illegal update of case type requested for {}", issueCase.getReceiptNumber());
				}
			}
		}

		// terminate all the remaining issues in the current collection
		// this could also be done directly in the database, which might not be a bad idea?
		currentMap.values().forEach(i -> i.setIssueClosed(eventDate));
		LOG.info("Closing {} issues", currentMap.size());
		translated.setClosedIssueCount(currentMap.size());

		// get or create cases
		HashSet<String> newReceipts = requestedNewIssues.stream().map(CaseRequest::getReceiptNumber)
				.collect(Collectors.toCollection(HashSet::new));
		Map<String, TroubleCase> existingCases = _caseRepo.getAllByCaseManagementSystemAndReceiptNumberIn(
				translated.getCaseManagementSystem(), newReceipts).stream().collect(Collectors.toMap(TroubleCase::getReceiptNumber, i->i));
		newReceipts.removeAll(existingCases.keySet());

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

		// aaaand save everything!
		_issueRepo.saveAll(
			Stream.concat(
				existingCases.values().stream().map(createIssue),
				newIssues.stream()
			).collect(Collectors.toSet())
		);
		translated.setNewIssueCount(existingCases.size() + newIssues.size());
		translated.setUploadStatus(UploadStatus.SUCCESSFUL);
		_uploadRepo.save(translated);
		return translated;
	}

	public DataFormatSpec getUploadFormat(String uploadFormatId) {
		if (uploadFormatId == null) {
			return new DataFormatSpec();
		}
		DataFormatSpec spec = _webProperties.getDataFormats().get(uploadFormatId);
		if (spec == null) {
			throw new IllegalArgumentException("Not a recognized data format");
		}
		return spec;
	}

	private List<CaseSummary> rewrap(List<Object[]> queryResult) {
		LOG.debug("Finding snoozed case from {}.", _snoozeRepo);
		LOG.debug("Finding attachment from {}.", _attachmentService);
		Function<? super Object[], ? extends CaseSummary> mapper = row ->{
			TroubleCase rootCase = (TroubleCase) row[0];
			ZonedDateTime lastSnoozeEnd = (ZonedDateTime) row[1];
			CaseSnoozeSummary summary = lastSnoozeEnd == null ? null
					: _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(rootCase).get();
			List<AttachmentSummary> notes = _attachmentService.findAttachmentsForCase(rootCase).stream().map(AttachmentSummary::new).collect(Collectors.toList());
			return new CaseSummaryImpl(rootCase, summary, notes);
		};
		return queryResult.stream().map(mapper).collect(Collectors.toList());
	}
}
