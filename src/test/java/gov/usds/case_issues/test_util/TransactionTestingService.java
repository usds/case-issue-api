package gov.usds.case_issues.test_util;


import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.services.UploadStatusService;

/**
 * Testing utility for doing tests that require nested transactions or controlled
 * failures inside a transaction boundary.
 */
@Service
@Transactional(readOnly=false)
public class TransactionTestingService {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionTestingService.class);

	public static final int ARBITRARY_UPLOAD_COUNT = 3823;
	@Autowired
	private UploadStatusService _statusService;
	@Autowired
	private FixtureDataInitializationService _dataService;
	@Autowired
	private CaseManagementSystemRepository _caseManagerRepo;

	public void initThenThrow(String systemTag) {
		_dataService.ensureCaseManagementSystemInitialized(systemTag, "The system named " + systemTag);
		throw new ExpectedException("Expected failure of method call");
	}

	public void fakeUploadFailure(CaseManagementSystem sys, CaseType caseType, String issueType) {
		LOG.info("Commencing fake upload");
		_statusService.commenceUpload(sys, caseType, issueType, ZonedDateTime.now(), ARBITRARY_UPLOAD_COUNT);
		LOG.info("Creating fake upload records");
		TroubleCase tc = _dataService.initCase(sys, "ABC123", caseType, ZonedDateTime.now());
		_dataService.initIssue(tc, issueType, ZonedDateTime.now(), null);
		throw new ExpectedException("Synthetic failure of fake upload process");
	}

	public boolean checkForCaseManagementSystem(String tag) {
		return _caseManagerRepo.findByExternalId(tag).isPresent();
	}

	/** An exception class to be thrown by our test methods */
	@SuppressWarnings("serial")
	public class ExpectedException extends RuntimeException {

		public ExpectedException(String string) {
			super(string);
		}
	}
}
