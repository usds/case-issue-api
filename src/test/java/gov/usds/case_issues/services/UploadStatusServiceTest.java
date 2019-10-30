package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseIssueUploadRepository;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.test_util.TransactionTestingService;
import gov.usds.case_issues.test_util.TransactionTestingService.ExpectedException;

@SuppressWarnings("checkstyle:MagicNumber")
public class UploadStatusServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private UploadStatusService _service;
	@Autowired
	private CaseIssueUploadRepository _repo;
	@Autowired
	private CaseIssueRepository _issueRepo;
	@Autowired
	private TransactionTestingService _transactionTester;

	// re-initialized for each test
	private CaseManagementSystem _sys;
	private CaseType _caseType;
	
	@Before
	public void clear() {
		truncateDb();
		_sys = _dataService.ensureCaseManagementSystemInitialized("YABBA", "Yet Another Big Bad Aggregator");
		_caseType = _dataService.ensureCaseTypeInitialized("DABBA", "Definitely Also Big Bad and Awesome");

	}

	@Test
	public void commenceUpload_validInput_recordCreated() {
		CaseIssueUpload uploadInfo = initUpload();
		assertEquals(UploadStatus.STARTED, uploadInfo.getUploadStatus());
		assertEquals(42, uploadInfo.getUploadedRecordCount());
		assertTrue(0 < uploadInfo.getInternalId());
		assertTrue(_repo.findById(uploadInfo.getInternalId()).isPresent());
	}

	@Test
	public void completeUpload_validInput_recordUpdated() {
		CaseIssueUpload uploadInfo = initUpload();
		Long id = uploadInfo.getInternalId();
		assertEquals(UploadStatus.STARTED, uploadInfo.getUploadStatus());
		assertNull(uploadInfo.getNewIssueCount());
		uploadInfo.setNewIssueCount(25L);
		uploadInfo.setClosedIssueCount(37);
		uploadInfo = _service.completeUpload(uploadInfo);
		assertEquals(id, uploadInfo.getInternalId());
		Optional<CaseIssueUpload> found = _repo.findById(id);
		assertTrue(found.isPresent());
		assertEquals(UploadStatus.SUCCESSFUL, found.get().getUploadStatus());
		assertNotNull(uploadInfo.getNewIssueCount());
		assertEquals(25L, uploadInfo.getNewIssueCount().longValue());
		assertEquals(37L, uploadInfo.getClosedIssueCount().longValue());
	}

	@Test
	public void failUpload_validInput_recordUpdated() {
		CaseIssueUpload uploadInfo = initUpload();
		_service.failUpload(uploadInfo);
		Optional<CaseIssueUpload> found = _repo.findById(uploadInfo.getInternalId());
		assertTrue(found.isPresent());
		assertEquals(UploadStatus.FAILED, found.get().getUploadStatus());
	}

	@Test
	public void readUploadInformation_validId_recordFound() {
		Long id = initUpload().getInternalId();
		CaseIssueUpload uploadInfo = _service.readUploadInformation(id);
		assertEquals(UploadStatus.STARTED, uploadInfo.getUploadStatus());
		assertEquals(42, uploadInfo.getUploadedRecordCount());
		assertTrue(0 < uploadInfo.getInternalId());
	}

	@Test(expected=IllegalArgumentException.class)
	public void readUploadInformation_invalidId_exception() {
		_service.readUploadInformation(10003L);
	}

	/** This doesn't follow the standard naming convention because it's not standard:
	 * we're just checking to make sure that the Spring transaction manager is working
	 * at all, so that if some wiring gets broken, there's a simple test that fails in
	 * a simple way, as well as many many other tests that fail in less simple ways.
	 */
	@Test
	public void transactionSanityTest() {
		boolean caught = false;
		try {
			_transactionTester.initThenThrow("HELLO");
		} catch (ExpectedException e) {
			caught = true; // this is mostly just to keep checkstyle happy, but it's not the worst sanity check ever.
		}
		assertTrue(caught);
		assertFalse(_transactionTester.checkForCaseManagementSystem("HELLO"));
	}

	@Test
	public void uploadSimulation_exceptionThrown_uploadRecordExists() {
		boolean caught = false;
		try {
			_transactionTester.fakeUploadFailure(_sys, _caseType, "WOTCHER");
		} catch (ExpectedException e) {
			caught = true; // this is mostly just to keep checkstyle happy, but it's not the worst sanity check ever.
		}
		assertTrue(caught);
		assertFalse("No issues should be found", _issueRepo.findAll().iterator().hasNext());
		List<CaseIssueUpload> uploads = _repo.findAllByCaseManagementSystemAndCaseType(_sys, _caseType);
		assertEquals("One upload record should be found", 1, uploads.size());
		CaseIssueUpload upload = uploads.get(0);
		assertEquals(UploadStatus.STARTED, upload.getUploadStatus());
		assertEquals(TransactionTestingService.ARBITRARY_UPLOAD_COUNT, upload.getUploadedRecordCount());
		assertNull(upload.getNewIssueCount());
		assertNull(upload.getClosedIssueCount());
	}

	private CaseIssueUpload initUpload() {
		return _service.commenceUpload(_sys, _caseType, "DOO", ZonedDateTime.now(), 42);
	}

	
}
