package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.services.model.CaseGroupInfo;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.test_util.MockConfig;

@ActiveProfiles(MockConfig.WRAPPED_REPOSITORIES_PROFILE)
@WithMockUser(authorities="UPDATE_ISSUES")
public class IssueUploadServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private IssueUploadService _uploadService;
	@Autowired
	private UploadStatusService _statusService;
	@Autowired
	private CaseIssueRepository _wrappedIssueRepo;
	
	private static final Long ZERO = Long.valueOf(0);

	private ZonedDateTime _now;
	private CaseManagementSystem _system;
	private CaseType _type;

	@Before
	public void reset() {
		truncateDb();
		_now = ZonedDateTime.now();
		_system = _dataService.ensureCaseManagementSystemInitialized("BIPPITY", "Fred", null);
		_type = _dataService.ensureCaseTypeInitialized("BOPPITY", "An IRS form", "Look it up");
	}

	@Test
	public void putIssueList_emptyList_expectedResult() {
		CaseIssueUpload uploaded = _uploadService.putIssueList(new CaseGroupInfo(_system, _type), "BOOP", Collections.emptyList(), _now);
		assertEquals(UploadStatus.SUCCESSFUL, uploaded.getUploadStatus());
		assertEquals(ZERO, uploaded.getNewIssueCount());
		assertEquals(ZERO, uploaded.getNewIssueCount());
		assertEquals(0, uploaded.getUploadedRecordCount());
		assertEquals(_now, uploaded.getEffectiveDate());
	}

	@Test
	public void putIssueList_exception_expectedResult() {
		Mockito.when(_wrappedIssueRepo.findActiveIssues(_system, _type, "BOOP"))
			.thenThrow(new IllegalArgumentException("check out this unchecked exception"));
		List<CaseRequest> requested = Collections.emptyList();
		CaseIssueUpload uploaded = _uploadService.putIssueList(new CaseGroupInfo(_system, _type), "BOOP", requested, _now);
		assertEquals(UploadStatus.FAILED, uploaded.getUploadStatus());
		CaseIssueUpload refetched = _statusService.readUploadInformation(uploaded.getInternalId());
		assertNotNull(refetched);
		assertEquals(UploadStatus.FAILED, refetched.getUploadStatus());
		assertNull(refetched.getClosedIssueCount());
		assertNull(refetched.getNewIssueCount());
	}
}
