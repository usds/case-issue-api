package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.repositories.CaseIssueUploadRepository;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@SuppressWarnings("checkstyle:MagicNumber")
public class UploadStatusServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private UploadStatusService _service;
	@Autowired
	private CaseIssueUploadRepository _repo;
	
	@Before
	public void clear() {
		truncateDb();
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
		uploadInfo = _service.completeUpload(uploadInfo, 25, 37);
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

	private CaseIssueUpload initUpload() {
		CaseManagementSystem sys = _dataService.ensureCaseManagementSystemInitialized("YABBA", "Yet Another Big Bad Aggregator");
		CaseType caseType = _dataService.ensureCaseTypeInitialized("DABBA", "Definitely Also Big Bad and Awesome");
		CaseIssueUpload uploadInfo = _service.commenceUpload(sys, caseType, "DOO", ZonedDateTime.now(), 42);
		return uploadInfo;
	}

	
}
