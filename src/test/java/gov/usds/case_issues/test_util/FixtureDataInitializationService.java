package gov.usds.case_issues.test_util;


import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;

@Service
@Transactional(readOnly=false)
public class FixtureDataInitializationService {

	private static final int DEFAULT_SNOOZE = 30;

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

	public CaseManagementSystem ensureCaseManagementSystemInitialized(String tag, String name, String description) {
		Optional<CaseManagementSystem> found = _caseManagementSystemRepo.findByCaseManagementSystemTag(tag);
		if (found.isPresent()) {
			return found.get();
		}
		return _caseManagementSystemRepo.save(new CaseManagementSystem(tag, name, description));
	}

	public CaseType ensureCaseTypeInitialized(String tag, String name, String description) {
		Optional<CaseType> found = _caseTypeRepository.findByCaseTypeTag(tag);
		if (found.isPresent()) {
			return found.get();
		}
		return _caseTypeRepository.save(new CaseType(tag, name, description));
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
}
