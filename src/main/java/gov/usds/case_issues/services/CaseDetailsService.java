package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.db.repositories.UserInformationRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseDetails;
import gov.usds.case_issues.model.CaseSnoozeSummaryFacade;
import gov.usds.case_issues.model.SnoozeRequest;

/**
 * Service object for querying and manipulating details of individual cases (largely manipulating the
 * snooze state, since issues are not intended to be handled through the browser-facing API).
 */
@Service
@Transactional(readOnly=true)
public class CaseDetailsService {

	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private UserInformationRepository _userRepo;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;
	@Autowired
	private CaseIssueRepository _issueRepo;
	@Autowired
	private CaseAttachmentService _attachmentService;

	public TroubleCase findCaseByTags(String caseManagementSystemTag, String receiptNumber) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByExternalId(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		TroubleCase mainCase = _caseRepo.findByCaseManagementSystemAndReceiptNumber(caseManagementSystem, receiptNumber)
				.orElseThrow(()->new ApiModelNotFoundException("Case", receiptNumber));
		return mainCase;
	}

	public Optional<CaseSnooze> findSnooze(String caseManagementSystemTag, String receiptNumber) {
		TroubleCase mainCase = findCaseByTags(caseManagementSystemTag, receiptNumber);
		return _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(mainCase);
	}

	/**
	 * Find all details about a case, using projection APIs that avoid circular references.
	 * @param caseManagementSystemTag
	 * @param receiptNumber
	 * @return
	 */
	public CaseDetails findCaseDetails(String caseManagementSystemTag, String receiptNumber) {
		TroubleCase mainCase = findCaseByTags(caseManagementSystemTag, receiptNumber);
		Collection<CaseIssueSummary> issues = _issueRepo.findAllByIssueCaseOrderByIssueCreated(mainCase);
		List<CaseSnoozeSummaryFacade> snoozes = _snoozeRepo.findAllBySnoozeCaseOrderBySnoozeStartAsc(mainCase).stream()
													.map(row -> new CaseSnoozeSummaryFacade(
														row,
														_userRepo.findByUserId(row.getCreatedBy())
													))
													.collect(Collectors.toList());
		List<AttachmentSummary> notes = _attachmentService.findAttachmentsForCase(mainCase).stream()
													.map(row -> new AttachmentSummary(
														row,
														 _userRepo.findByUserId(row.getCreatedBy())
													))
													.collect(Collectors.toList());
		return new CaseDetails(mainCase, issues, snoozes, notes);
	}

	public Optional<CaseSnoozeSummary> findActiveSnooze(String caseManagementSystemTag, String receiptNumber) {
		Optional<CaseSnooze> found = findSnooze(caseManagementSystemTag, receiptNumber);
		if (snoozeIsActive(found)) {
			return Optional.of(new CaseSnoozeSummaryFacade(found));
		} else {
			return Optional.empty();
		}
	}

	@Transactional(readOnly=false)
	public boolean endActiveSnooze(String caseManagementSystemTag, String receiptNumber) {
		Optional<CaseSnooze> found = findSnooze(caseManagementSystemTag, receiptNumber);
		if (snoozeIsActive(found)) {
			found.get().endSnoozeNow();
			return true;
		} else {
			return false;
		}
	}

	@Transactional(readOnly=false)
	public CaseSnoozeSummaryFacade updateSnooze(String caseManagementSystemTag, String receiptNumber, @Valid SnoozeRequest requestedSnooze) {
		TroubleCase mainCase = findCaseByTags(caseManagementSystemTag, receiptNumber);
		Optional<CaseSnooze> foundSnooze = _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(mainCase);
		if (snoozeIsActive(foundSnooze)) {
			CaseSnooze oldSnooze = foundSnooze.get();
			oldSnooze.endSnoozeNow();
		}
		String reason = requestedSnooze.getSnoozeReason();
		int duration = requestedSnooze.getDuration();
		CaseSnooze replacement = new CaseSnooze(mainCase, reason, duration);
		_snoozeRepo.save(replacement);
		List<AttachmentSummary> savedNotes = requestedSnooze.getNotes().stream()
				.map(r->_attachmentService.attachToSnooze(r, replacement))
				.map(AttachmentSummary::new)
				.collect(Collectors.toList());
		return new CaseSnoozeSummaryFacade(replacement, savedNotes);
	}

	private static boolean snoozeIsActive(Optional<CaseSnooze> snooze) {
		return snooze.isPresent() && snooze.get().getSnoozeEnd().isAfter(ZonedDateTime.now());
	}

	@Transactional(readOnly=false)
	public CaseAttachmentAssociation annotateActiveSnooze(String caseManagementSystemTag, String receiptNumber, AttachmentRequest newNote) {
		TroubleCase mainCase = findCaseByTags(caseManagementSystemTag, receiptNumber);
		Optional<CaseSnooze> foundSnooze = _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(mainCase);
		if (!snoozeIsActive(foundSnooze)) {
			throw new IllegalArgumentException("Cannot add a note to a case that is not snoozed.");
		}
		return _attachmentService.attachToSnooze(newNote, foundSnooze.get());
	}
}
