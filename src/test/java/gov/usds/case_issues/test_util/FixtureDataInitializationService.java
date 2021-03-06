package gov.usds.case_issues.test_util;


import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.AttachmentSubtype;
import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.AttachmentSubtypeRepository;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;

/**
 * Utility service for creating valid entities for testing purposes.
 */
@Service
@Transactional(readOnly=false)
public class FixtureDataInitializationService {

	private static final int DEFAULT_SNOOZE = 30;

	private static final Logger LOG = LoggerFactory.getLogger(FixtureDataInitializationService.class);

	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private CaseTypeRepository _caseTypeRepository;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private CaseIssueRepository _issueRepo;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;
	@Autowired
	private AttachmentSubtypeRepository _subtypeRepo;

	public CaseManagementSystem ensureCaseManagementSystemInitialized(String tag, String name) {
		return ensureCaseManagementSystemInitialized(tag, name, null);
	}

	public boolean checkForCaseManagementSystem(String tag, Instant expires) {
		LOG.debug("Checking that {} was created not before {}", tag, expires);
		Optional<CaseManagementSystem> found = _caseManagementSystemRepo.findByExternalId(tag);
		return found.isPresent() && found.get().getCreatedAt().toInstant().isAfter(expires);
	}

	public CaseManagementSystem ensureCaseManagementSystemInitialized(String tag, String name, String description) {
		LOG.debug("(Re)initializing case management system '{}'", tag);
		Optional<CaseManagementSystem> found = _caseManagementSystemRepo.findByExternalId(tag);
		if (found.isPresent()) {
			return found.get();
		}
		return _caseManagementSystemRepo.save(new CaseManagementSystem(tag, name, description));
	}

	public CaseType ensureCaseTypeInitialized(String tag, String name) {
		return ensureCaseTypeInitialized(tag, name, null);
	}

	public CaseType ensureCaseTypeInitialized(String tag, String name, String description) {
		LOG.debug("(Re)initializing case type '{}'", tag);
		Optional<CaseType> found = _caseTypeRepository.findByExternalId(tag);
		if (found.isPresent()) {
			return found.get();
		}
		return _caseTypeRepository.save(new CaseType(tag, name, description));
	}

	public AttachmentSubtype ensureAttachmentSubtypeInitialized(String tag, String name, AttachmentType forType, String urlTempate) {
		LOG.debug("(Re)initializing attachment subtype '{}'", tag);
		Optional<AttachmentSubtype> found = _subtypeRepo.findByExternalId(tag);
		if (found.isPresent()) {
			AttachmentSubtype subtype = found.get();
			if (subtype.getForAttachmentType() != forType) {
				throw new IllegalArgumentException("Conflicting definitions for attachment subtype " + tag);
			}
			return subtype;
		}
		return _subtypeRepo.save(new AttachmentSubtype(tag, forType, name, "Auto-subtype " + name, urlTempate));
	}

	public TroubleCase initCase(CaseManagementSystem caseManagementSystem, String receiptNumber, CaseType caseType, ZonedDateTime caseCreation,
			String... furtherArgs) {
		if (0 != furtherArgs.length % 2) {
			throw new IllegalArgumentException("Extra data arguments to initCase must be an even number");
		}
		Map<String, Object> extraData = new HashMap<>();
		for (int i = 0; i < furtherArgs.length; i += 2) {
			extraData.put(furtherArgs[i], furtherArgs[i+1]);
		}
		return _caseRepo.save(new TroubleCase(caseManagementSystem, receiptNumber, caseType, caseCreation, extraData ));
	}

	public TroubleCase initCaseAndOpenIssue(CaseManagementSystem caseManagementSystem, String receiptNumber, CaseType caseType,  ZonedDateTime caseCreation,
			String issueType, String... keyValueData) {
		return initCaseAndIssue(caseManagementSystem, receiptNumber, caseType, caseCreation, issueType, null, keyValueData);
	}

	public TroubleCase initCaseAndIssue(CaseManagementSystem caseManagementSystem, String receiptNumber, CaseType caseType,  ZonedDateTime caseCreation,
			String issueType, ZonedDateTime issueClosedDate, String... keyValueData) {
		TroubleCase c = initCase(caseManagementSystem, receiptNumber, caseType, caseCreation, keyValueData);
		initIssue(c, issueType, caseCreation, issueClosedDate);
		return c;
	}
	/** Create an open issue for this case, with the same creation date as the case itself */
	public CaseIssue initOpenIssue(TroubleCase troubleCase, String issueType) {
		return initIssue(troubleCase, issueType, troubleCase.getCaseCreation(), null);
	}

	/** Create an open issue for this case */
	public CaseIssue initOpenIssue(TroubleCase troubleCase, String issueType, ZonedDateTime issueStart) {
		return initIssue(troubleCase, issueType, issueStart, null);
	}

	/** Create an issue for this case with the given open and close dates */
	public CaseIssue initIssue(TroubleCase troubleCase, String issueType, ZonedDateTime issueStart, ZonedDateTime issueEnd) {
		CaseIssue issue = new CaseIssue(troubleCase, issueType, issueStart);
		if (issueEnd != null) {
			issue.setIssueClosed(issueEnd);
		}
		return _issueRepo.save(issue);
	}

	public CaseSnooze snoozeCase(TroubleCase troubleCase) {
		return _snoozeRepo.save(new CaseSnooze(troubleCase, "DONOTCARE", DEFAULT_SNOOZE));
	}

	public CaseSnooze snoozeCase(TroubleCase tc, String snoozeReason, int requestedDays, boolean cancel) {
		CaseSnooze snzed = new CaseSnooze(tc, snoozeReason, requestedDays);
		if (cancel) {
			snzed.endSnoozeNow();
		}
		return _snoozeRepo.save(snzed);
	}
}
