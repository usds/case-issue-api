package gov.usds.case_issues.services;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import gov.usds.case_issues.config.DataFormatSpec;
import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@SuppressWarnings("checkstyle:MagicNumber")
public class CaseListServiceTest extends CaseIssueApiTestBase {

	private static final String VALID_TYPE_TAG = "1040E-Z";
	private static final String VALID_SYS_TAG = "ABCDE";

	@Autowired
	private CaseListService _service;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private CaseIssueRepository _issueRepo;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private ZonedDateTime _now;
	private CaseManagementSystem _system;
	private CaseType _type;

	@Before
	public void reset() {
		truncateDb();
		_now = ZonedDateTime.now();
		_system = _dataService.ensureCaseManagementSystemInitialized(VALID_SYS_TAG, "Fred", null);
		_type = _dataService.ensureCaseTypeInitialized(VALID_TYPE_TAG, "An IRS form", "Look it up");
	}

	@Test
	public void translatePath_invalidCaseManagementSystem_notFoundError() {
		String badId = "NO_SUCH_SYSTEM";
		expected.expect(allOf(
				isA(ApiModelNotFoundException.class),
				hasProperty("entityType", equalTo("Case Management System")),
				hasProperty("entityId", equalTo(badId))
		));
		_service.translatePath(badId, "UNCHECKED");
	}

	@Test
	public void translatePath_improperCaseManagementSystemTag_validationError() {
		expected.expect(ConstraintViolationException.class);
		_service.translatePath("?", VALID_TYPE_TAG);
	}

	@Test
	public void translatePath_improperCaseTypeTag_validationError() {
		expected.expect(ConstraintViolationException.class);
		_service.translatePath(VALID_SYS_TAG, "hello\nthere");
	}

	@Test
	public void translatePath_invalidCaseType_notFoundError() {
		String badId = "NOBODY-LOVES-YOU";
		_dataService.ensureCaseManagementSystemInitialized(VALID_SYS_TAG, "Totes Real", "A genuine record in the DB!");
		expected.expect(allOf(
				isA(ApiModelNotFoundException.class),
				hasProperty("entityType", equalTo("Case Type")),
				hasProperty("entityId", equalTo(badId))
		));
		_service.translatePath(VALID_SYS_TAG, badId);
	}

	@Test
	public void translatePath_validPath_itemsFound() {
		CaseGroupInfo translated = _service.translatePath(VALID_SYS_TAG, VALID_TYPE_TAG);
		assertEquals("Case management system ID", _system.getInternalId(), translated.getCaseManagementSystemId());
		assertEquals("Case type ID", _type.getInternalId(), translated.getCaseTypeId());
	}

	@Test
	public void getCases_noQuery_noCasesReturned() {
		List<TroubleCase> cases = _service.getCases(VALID_SYS_TAG, VALID_TYPE_TAG, "");
		assertEquals(0, cases.size());
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_invalidSystemTag_exception() {
		_service.getCases("hello\nworld", VALID_TYPE_TAG, "Woof");
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_invalidTypeTag_exception() {
		_service.getCases(VALID_SYS_TAG, "hello\nworld", "Woof");
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSummaryInfo_invalidSystemTag_exception() {
		_service.getSummaryInfo("hello\nworld", VALID_TYPE_TAG);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSummaryInfo_invalidTypeTag_exception() {
		_service.getSummaryInfo(VALID_SYS_TAG, "hello\nworld");
	}

	@Test
	public void getCases_exactReceiptNumber_returnsCaseWithQueriedReceiptNumber() {
		String receiptNumber = "ABC1234567";

		CaseGroupInfo translated = _service.translatePath(VALID_SYS_TAG, VALID_TYPE_TAG);
		_dataService.initCase(
			translated.getCaseManagementSystem(),
			receiptNumber,
			translated.getCaseType(),
			_now
		);
		_dataService.initCase(
			translated.getCaseManagementSystem(),
			"XYZ8901234",
			translated.getCaseType(),
			_now
		);

		List<TroubleCase> cases = _service.getCases(VALID_SYS_TAG, VALID_TYPE_TAG, receiptNumber);

		assertEquals(1, cases.size());
		assertEquals(receiptNumber, cases.get(0).getReceiptNumber());
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_invalidSystemTag_exception() {
		_service.getActiveCases("hello\nworld", VALID_TYPE_TAG, null, 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_invalidTypeTag_exception() {
		_service.getActiveCases(VALID_SYS_TAG, "hello\nworld", null, 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_invalidReceipt_exception() {
		_service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, "/etc/passwd", 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_excessivePageSize_exception() {
		_service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, null, 101);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_excessivePageSizeSecondPage_exception() {
		String receipt = "ABCDE";
		_dataService.initCase(_system, receipt, _type, _now);
		_service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, receipt, 101);
	}

	@Test
	public void getActiveCases_zeroPageSize_emptyList() {
		String receipt = "ABCDE";
		_dataService.initCase(_system, receipt, _type, _now);
		assertEquals(0, _service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, null, 0).size());
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_negativePageSize_exception() {
		String receipt = "ABCDE";
		_dataService.initCase(_system, receipt, _type, _now);
		assertEquals(0, _service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, null, -10).size());
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_negativePageSizeSecondPage_exception() {
		String receipt = "ABCDE";
		_dataService.initCase(_system, receipt, _type, _now);
		assertEquals(0, _service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, receipt, -10).size());
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_invalidSystemTag_exception() {
		_service.getSnoozedCases("hello\nworld", VALID_TYPE_TAG, null, 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_invalidTypeTag_exception() {
		_service.getSnoozedCases(VALID_SYS_TAG, "hello\nworld", null, 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_invalidReceipt_exception() {
		_service.getSnoozedCases(VALID_SYS_TAG, VALID_TYPE_TAG, "/etc/passwd", 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_excessivePageSize_exception() {
		_service.getSnoozedCases(VALID_SYS_TAG, VALID_TYPE_TAG, null, 101);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_excessivePageSizeSecondPage_exception() {
		String receipt = "ABCDE";
		_dataService.snoozeCase(_dataService.initCase(_system, receipt, _type, _now));
		_service.getSnoozedCases(VALID_SYS_TAG, VALID_TYPE_TAG, receipt, 101);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_negativePageSize_exception() {
		String receipt = "ABCDE";
		_dataService.snoozeCase(_dataService.initCase(_system, receipt, _type, _now));
		_service.getSnoozedCases(VALID_SYS_TAG, VALID_TYPE_TAG, null, -1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_negativePageSizeSecondPage_exception() {
		String receipt = "ABCDE";
		_dataService.snoozeCase(_dataService.initCase(_system, receipt, _type, _now));
		_service.getSnoozedCases(VALID_SYS_TAG, VALID_TYPE_TAG, receipt, -1);
	}

	@Test
	public void getSnoozedCases_zeroPageSize_emptyList() {
		String receipt = "ABCDE";
		_dataService.snoozeCase(_dataService.initCase(_system, receipt, _type, _now));
		assertEquals(0, _service.getSnoozedCases(VALID_SYS_TAG, VALID_TYPE_TAG, null, 0).size());
	}

	@Test
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void putIssueList_noIssuesNoInput_nothingTerribleHappens() {
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, "SUPER-OLD", Collections.emptyList(), _now);
	}

	@Test
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void putIssueList_blankSlateNewCases_casesCreated() {
		List<CaseRequest> newIssueCases = new ArrayList<>();
		newIssueCases.add(new CaseRequestImpl("A123"));
		newIssueCases.add(new CaseRequestImpl("A124"));
		String issueType = "SUPER-OLD";
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueType, newIssueCases, _now);

		Map<String, TroubleCase> allCases = new HashMap<>();
		_caseRepo.findAll().forEach(tc -> allCases.put(tc.getReceiptNumber(), tc));

		assertEquals("two cases were found", 2, allCases.size());
		assertTrue("Found A123", allCases.containsKey("A123"));
		assertTrue("Found A124", allCases.containsKey("A124"));
		List<CaseIssue> openIssues = allCases.get("A123").getOpenIssues();
		assertEquals(1, openIssues.size());
		assertEquals(issueType, openIssues.get(0).getIssueType());
	}

	@Test
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void putIssueList_existingCasesNewIssues_issuesCreated() {
		String issueType = "ANCIENT";
		_dataService.initCase(_system, "A1", _type, _now);
		_dataService.initCase(_system, "A2", _type, _now);
		_dataService.initCase(_system, "A3", _type, _now);
		_dataService.initCase(_system, "A4", _type, _now);
		List<String> issueReceipts = Arrays.asList("A1","A2","A3");
		List<CaseRequest> newIssueCases = issueReceipts.stream()
				.map(CaseRequestImpl::new).collect(Collectors.toList());
		CaseIssueUpload uploadInfo = wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueType, newIssueCases, _now.minusDays(1));
		assertEquals(3, uploadInfo.getUploadedRecordCount());
		assertEquals(Long.valueOf(3), uploadInfo.getNewIssueCount());
		assertEquals(Long.valueOf(0), uploadInfo.getClosedIssueCount());
		assertEquals(UploadStatus.SUCCESSFUL, uploadInfo.getUploadStatus());
		for (String receipt : issueReceipts) {
			Optional<TroubleCase> mainCase = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, receipt);
			List<CaseIssueSummary> issues = _issueRepo.findAllByIssueCaseOrderByIssueCreated(mainCase.get());
			assertEquals("issue count for " + receipt, 1, issues.size());
			CaseIssueSummary issueSummary = issues.get(0);
			assertEquals("issue type for " + receipt, issueType, issueSummary.getIssueType());
			assertEquals("issue created date for " + receipt, _now.minusDays(1), issueSummary.getIssueCreated());
			assertNull("issue closed date for " + receipt, issueSummary.getIssueClosed());
		}
	}

	@Test
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void putIssueList_existingIssuesEmptyInput_issuesClosed() {
		ZonedDateTime then = _now.minusMonths(1);
		String myIssueType = "BADNESS";
		_dataService.initOpenIssue(_dataService.initCase(_system, "A1", _type, then), myIssueType);
		_dataService.initOpenIssue(_dataService.initCase(_system, "A2", _type, then), myIssueType);
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, myIssueType, Collections.emptyList(), _now);
		_issueRepo.findAll().forEach(i -> assertEquals(_now, i.getIssueClosed()));
		_caseRepo.findAll().forEach(c -> assertTrue(c.getOpenIssues().isEmpty()));
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void putIssueList_existingIssuesNewCaseData_casesUpdated() {
		ZonedDateTime then = _now.minusMonths(1);
		String myIssueType = "SQUIRREL";
		_dataService.initOpenIssue(_dataService.initCase(_system, "A1", _type, then, "state", "original", "ignored", "old"), myIssueType);
		_dataService.initOpenIssue(_dataService.initCase(_system, "A2", _type, then, "state", "original", "ignored", "old"), myIssueType);

		Map<String, Object> newA1Data = new HashMap<>();
		newA1Data.put("state", "updated");
		newA1Data.put("newthing", "exists");
		Map<String, Object> newA2Data = new HashMap<>();
		newA2Data.put("state", "updated");

		List<CaseRequest> newIssues = Arrays.asList(new CaseRequestImpl("A1", newA1Data), new CaseRequestImpl("A2", newA2Data));
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, myIssueType, newIssues, _now);
		Map<String, Object> foundData = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, "A1").get().getExtraData();
		assertEquals("old", foundData.get("ignored"));
		assertEquals("updated", foundData.get("state"));
		assertEquals("exists", foundData.get("newthing"));
		assertEquals(3, foundData.size());
		foundData = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, "A2").get().getExtraData();
		assertEquals("old", foundData.get("ignored"));
		assertEquals("updated", foundData.get("state"));
		assertEquals(2, foundData.size());

	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void putIssueList_existingCasesNewCaseData_casesUpdated() {
		ZonedDateTime then = _now.minusMonths(1);
		String myIssueType = "INIMITABLE";
		_dataService.initCase(_system, "B1", _type, then, "state", "original", "ignored", "old");
		_dataService.initCase(_system, "B2", _type, then, "state", "original", "ignored", "old");

		Map<String, Object> newB1Data = new HashMap<>();
		newB1Data.put("state", "updated");
		newB1Data.put("newthing", "exists");
		Map<String, Object> newB2Data = new HashMap<>();
		newB2Data.put("state", "updated");
		List<CaseRequest> newIssues = Arrays.asList(new CaseRequestImpl("B1", newB1Data), new CaseRequestImpl("B2", newB2Data));
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, myIssueType, newIssues, _now);

		Map<String, Object> foundData = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, "B1").get().getExtraData();
		assertEquals("old", foundData.get("ignored"));
		assertEquals("updated", foundData.get("state"));
		assertEquals("exists", foundData.get("newthing"));
		assertEquals(3, foundData.size());

		foundData = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, "B2").get().getExtraData();
		assertEquals("old", foundData.get("ignored"));
		assertEquals("updated", foundData.get("state"));
		assertEquals(2, foundData.size());
	}

	@Test(expected=AccessDeniedException.class)
	@WithMockUser
	public void putIssueList_unauthorizedUser_exception() {
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, "UNCHECKED", Collections.emptyList(), _now);
	}

	@Test(expected=AccessDeniedException.class)
	@WithMockUser(authorities="UPDATE_CASES")
	public void putIssueList_insufficientlyAuthorizedUser_exception() {
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, "UNCHECKED", Collections.emptyList(), _now);
	}

	@Test
	// just ... exercise a bunch of things and make sure things come out right, to determine
	@SuppressWarnings("checkstyle:MagicNumber")
	@WithMockUser(authorities="UPDATE_ISSUES")
	public void omnibusListOperationsTest() {
		String checkKey = "requestedAs";
		CaseRequest a = new CaseRequestImpl("C1", Collections.singletonMap(checkKey, "C1"));
		CaseRequest b = new CaseRequestImpl("C2", Collections.singletonMap(checkKey, "C2"));
		CaseRequest c = new CaseRequestImpl("C3", Collections.singletonMap(checkKey, "C3"));
		String otherSystem = "DIFFERENT_ONE";
		String issueTypeA = "BACON";
		String issueTypeB = "SAUSAGE";
		String issueTypeC = "HAM";

		_dataService.ensureCaseManagementSystemInitialized(otherSystem, "A different system");
		List<CaseSummary> activeCases = fetchCasesForSystem(VALID_SYS_TAG);
		assertEquals("no active cases in " + VALID_SYS_TAG + " at start", 0, activeCases.size());
		activeCases = fetchCasesForSystem(otherSystem);
		assertEquals("no active cases in " + otherSystem + " at start", 0, activeCases.size());
		wrappedPutIssueList(otherSystem, VALID_TYPE_TAG, issueTypeA, Arrays.asList(a,b,c), _now.minusHours(1));
		assertEquals("No active cases for " + VALID_SYS_TAG, 0, fetchCasesForSystem(VALID_SYS_TAG).size());
		assertEquals("3 active cases for " + otherSystem, 3, fetchCasesForSystem(otherSystem).size());

		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueTypeA, Arrays.asList(a, b), _now.minusDays(3));
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueTypeB, Arrays.asList(b, c), _now.minusDays(2));
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueTypeC, Arrays.asList(b, c), _now.minusDays(1));
		assertEquals("3 active cases for " + VALID_SYS_TAG, 3, fetchCasesForSystem(VALID_SYS_TAG).size());

		// spot check
		TroubleCase foundCase = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, "C2").get();
		List<CaseIssueSummary> issues = _issueRepo.findAllByIssueCaseOrderByIssueCreated(foundCase);
		assertEquals("three issues for C2", 3, issues.size());
		issues.forEach(i -> assertNull("Issue not closed for C2: " + i.getIssueType(), i.getIssueClosed()));

		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueTypeA, Collections.singletonList(b), _now);
		List<CaseSummary> nowActive = fetchCasesForSystem(VALID_SYS_TAG);
		assertEquals("One case is gone", 2, nowActive.size());
		Set<String> activeReceipts = nowActive.stream().map(ac -> ac.getReceiptNumber()).collect(Collectors.toSet());
		assertTrue("C2 still active", activeReceipts.contains("C2"));
		assertTrue("C3 still active", activeReceipts.contains("C3"));
		foundCase = _caseRepo.findByCaseManagementSystemAndReceiptNumber(_system, "C1").get();
		issues = _issueRepo.findAllByIssueCaseOrderByIssueCreated(foundCase);
		assertEquals("issue type", issueTypeA, issues.get(0).getIssueType());
		assertEquals("issue closed date", _now, issues.get(0).getIssueClosed());
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueTypeB, Collections.emptyList(), _now);
		assertEquals("Still two cases", 2, fetchCasesForSystem(VALID_SYS_TAG).size());
		wrappedPutIssueList(VALID_SYS_TAG, VALID_TYPE_TAG, issueTypeC, Collections.emptyList(), _now);
		activeCases = fetchCasesForSystem(VALID_SYS_TAG);
		assertEquals("Just one active case", 1, activeCases.size());
		assertEquals("C2", activeCases.get(0).getReceiptNumber());
		assertEquals("Checking extra data", "C2", activeCases.get(0).getExtraData().get(checkKey));
		assertEquals("Other list remains intact", 3, fetchCasesForSystem(otherSystem).size());
	}

	@Test()
	public void getUploadFormat_formatIdNull_DefaultFormat() {
		DataFormatSpec uploadFormat = _service.getUploadFormat(null);

		assertEquals(
			"Default creation date was set",
			DataFormatSpec.DEFAULT_CREATION_DATE_KEY,
			uploadFormat.getCreationDateKey()
		);
		assertEquals(
			"Default receipt number was set",
			DataFormatSpec.DEFAULT_RECEIPT_NUMBER_KEY,
			uploadFormat.getReceiptNumberKey()
		);
		assertEquals(
			"Default datetime format was set",
			DataFormatSpec.DEFAULT_DATETIME_FORMAT,
			uploadFormat.getCreationDateParser()
		);
	}

	@Test(expected=IllegalArgumentException.class)
	public void getUploadFormat_formatIdInvalid_exception() {
		_service.getUploadFormat("INVALID DATE FORMAT");
	}

	// if this test breaks during a re-build of the paging feature, delete it and re-implement it in CaseListPagingFilteringTest
	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void getActiveCases_addedNewestFirst_paginatedCorrectly() {
		String oldestReceiptNumber = "FKE7487700";
		TroubleCase a = _dataService.initCase(_system, "FKE3742810", _type, ZonedDateTime.parse("2018-08-29T00:00:00-04:00"));
		TroubleCase b = _dataService.initCase(_system, "FKE7209266", _type, ZonedDateTime.parse("2017-08-29T00:00:00-04:00"));
		TroubleCase c = _dataService.initCase(_system, oldestReceiptNumber, _type, ZonedDateTime.parse("2016-08-29T00:00:00-04:00"));
		ZonedDateTime lastMonth = ZonedDateTime.now().minusMonths(1);
		_dataService.initIssue(a, "FOOBAR", lastMonth, null);
		_dataService.initIssue(b, "FOOBAR", lastMonth, null);
		_dataService.initIssue(c, "FOOBAR", lastMonth, null);

		List<CaseSummary> activeCases = _service.getActiveCases(VALID_SYS_TAG, VALID_TYPE_TAG, oldestReceiptNumber, 20);
		assertEquals("The two newer cases should be returned", 2, activeCases.size());
	}

	private CaseIssueUpload wrappedPutIssueList(String systemTag, String caseTypeTag, String issueTypeTag,
			List<CaseRequest> newIssueCases, ZonedDateTime eventDate) {
		CaseGroupInfo translated = _service.translatePath(systemTag, caseTypeTag);
		CaseIssueUpload uploadInfo = new CaseIssueUpload(translated.getCaseManagementSystem(),
		    translated.getCaseType(), issueTypeTag, eventDate, newIssueCases.size());
		return _service.putIssueList(uploadInfo, newIssueCases);
	}

	@SuppressWarnings("checkstyle:MagicNumber")
	private List<CaseSummary> fetchCasesForSystem(String systemTag) {
		 return _service.getActiveCases(systemTag, VALID_TYPE_TAG, null, 20);
	}

	private class CaseRequestImpl implements CaseRequest {

		private String _receiptNumber;
		private Map<String, Object> _extraData;

		public CaseRequestImpl(String receipt) {
			this(receipt, Collections.emptyMap());
		}

		public CaseRequestImpl(String receipt, Map<String, Object> extraData) {
			_receiptNumber = receipt;
			_extraData = extraData;
		}

		@Override
		public String getReceiptNumber() {
			return _receiptNumber;
		}


		@Override
		public ZonedDateTime getCaseCreation() {
			return _now;
		}

		@Override
		public Map<String, Object> getExtraData() {
			return _extraData;
		}

	}
}
