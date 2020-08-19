package gov.usds.case_issues.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static gov.usds.case_issues.controllers.ApiTests.doSearch;
import static gov.usds.case_issues.controllers.ApiTests.doGetCases;
import static gov.usds.case_issues.controllers.ApiTests.putIssues;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.model.CaseDetails;
import gov.usds.case_issues.services.CaseDetailsService;
import gov.usds.case_issues.services.UploadStatusService;

/**
 * Tests of the Hitlist API behavior that include actual database calls.
 */
@WithMockUser(username = "default_hitlist_user", authorities = "READ_CASES")
@SuppressWarnings("checkstyle:MagicNumber")
public class HitlistApiControllerIntegrationTest extends ControllerTestBase {

	private static final String CSV_HEADER_SHORT = "receiptNumber,creationDate,channelType\n";

	private static final String DATE_STAMP_2018 = "2018-01-01T12:00:00Z";
	private static final String DATE_STAMP_2019 = "2019-01-01T12:00:00Z";
	private static final String VALID_ISSUE_TYPE = ApiTests.VALID_ISSUE_TYPE;
	private static final String VALID_CASE_TYPE = ApiTests.VALID_CASE_TYPE;
	private static final String VALID_CASE_MGT_SYS = ApiTests.VALID_CASE_MGT_SYS;
	private static final String CASE_TYPE_NOPE = "Case Type 'NOPE' was not found";
	private static final String CASE_MANAGEMENT_SYSTEM_NOPE = "Case Management System 'NOPE' was not found";

	private CaseManagementSystem _system;
	private CaseType _type;

	@Autowired
	private UploadStatusService _uploadService;
	@Autowired
	private CaseDetailsService _detailsService;
	
	@Before
	public void resetDb() {
		truncateDb();
		_system = _dataService.ensureCaseManagementSystemInitialized(VALID_CASE_MGT_SYS, "Fake 1", "Fakest");
		_type = _dataService.ensureCaseTypeInitialized(VALID_CASE_TYPE, "Case type 1", "");
	}

	@Test
	public void invalidPath_correctErrorMessages() throws Exception {
		_mvc.perform(getActive("NOPE", VALID_CASE_TYPE))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(CASE_MANAGEMENT_SYSTEM_NOPE))
		;
		_mvc.perform(getSnoozed("NOPE", VALID_CASE_TYPE))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(CASE_MANAGEMENT_SYSTEM_NOPE))
		;
		_mvc.perform(getSummary("NOPE", VALID_CASE_TYPE))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(CASE_MANAGEMENT_SYSTEM_NOPE))
		;
		_mvc.perform(getActive(VALID_CASE_MGT_SYS, "NOPE"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(CASE_TYPE_NOPE))
		;
		_mvc.perform(getSnoozed(VALID_CASE_MGT_SYS, "NOPE"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(CASE_TYPE_NOPE))
		;
		_mvc.perform(getSummary(VALID_CASE_MGT_SYS, "NOPE"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(CASE_TYPE_NOPE))
		;
	}

	@Test
	public void validPath_noData_emptyResponses() throws Exception {
		_mvc.perform(getActive(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		_mvc.perform(getSnoozed(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		_mvc.perform(getSummary(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.NEVER_SNOOZED").doesNotExist())
			.andExpect(jsonPath("$.CURRENTLY_SNOOZED").doesNotExist())
			.andExpect(jsonPath("$.PREVIOUSLY_SNOOZED").doesNotExist())
		;
	}

	@Test
	public void getActive_withData_correctResponse() throws Exception {
		initCaseData();
		_mvc.perform(getSummary(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.NEVER_SNOOZED").value("1"))
			.andExpect(jsonPath("$.CURRENTLY_SNOOZED").value("1"))
		;
		_mvc.perform(getActive(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("[{'receiptNumber': 'FFFF1111'}]", false))
			.andExpect(jsonPath("$[0].snoozeInformation").value(Matchers.nullValue()))
		;
		_mvc.perform(getSnoozed(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("[{'receiptNumber': 'FFFF1112', 'snoozeInformation': {'snoozeReason': 'DONOTCARE'}}]", false))
			.andExpect(jsonPath("$[0].snoozeInformation").isMap())
		;
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putJson_emptyList_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues(MediaType.APPLICATION_JSON_VALUE)
			.content("[]");
		perform(jsonPut).andExpect(status().isAccepted());
		checkUploadRecord(0, 0, 0);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_emptyList_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues(ApiTests.CSV_CONTENT)
			.content("header1,header2\n");
		perform(jsonPut).andExpect(status().isAccepted());
		checkUploadRecord(0, 0, 0);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_singleCase_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues(ApiTests.CSV_CONTENT)
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,2014-08-29T00:00:00-04:00,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
			);
		perform(jsonPut).andExpect(status().isAccepted());
		checkUploadRecord(1, 1, 0);
	}


	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putCsv_backDatedIssues_correctDateUsed() throws Exception {
		String receiptNumber = "FKE27182818";
		String effectiveDate = "2017-05-15T20:00:00Z";
		MockHttpServletRequestBuilder issuePut = putIssues(ApiTests.CSV_CONTENT, effectiveDate)
			.content(CSV_HEADER_SHORT + receiptNumber + ",2001-08-29T00:00:00-04:00,Pay Per View\n");
		perform(issuePut).andExpect(status().isAccepted());
		CaseIssueUpload lastUpload = _uploadService.getLastUpload(_system, _type, VALID_ISSUE_TYPE).get();
		assertDateEquals("CSV put effective date", effectiveDate, lastUpload.getEffectiveDate());
		CaseDetails details = _detailsService.findCaseDetails(VALID_CASE_MGT_SYS, receiptNumber);
		Optional<? extends CaseIssueSummary> optIssue = details.getIssues().stream().findFirst();
		assertTrue("Issue exists", optIssue.isPresent());
		assertEquals(lastUpload.getEffectiveDate(), optIssue.get().getIssueCreated());
	}

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putCsv_backDatedIssuesClosed_issueDatesCorrected() throws Exception {
		String receiptNumber = "FKE27182818";
		String startDateString = "2010-05-15T20:00:00Z";
		MockHttpServletRequestBuilder issuePut = putIssues(ApiTests.CSV_CONTENT, startDateString)
			.content(CSV_HEADER_SHORT + receiptNumber + ",1978-08-29T00:00:00-04:00,Broadcast\n");
		perform(issuePut).andExpect(status().isAccepted());
		String endDateString = "2011-04-30T20:00:00Z";
		issuePut = putIssues(ApiTests.CSV_CONTENT, endDateString)
			.content(CSV_HEADER_SHORT);
		perform(issuePut).andExpect(status().isAccepted());
		CaseDetails details = _detailsService.findCaseDetails(VALID_CASE_MGT_SYS, receiptNumber);
		Optional<? extends CaseIssueSummary> optIssue = details.getIssues().stream().findFirst();
		assertTrue("Issue exists", optIssue.isPresent());
		assertDateEquals("issue creation backdate", startDateString, optIssue.get().getIssueCreated());
		assertDateEquals("issue closure backdate", endDateString, optIssue.get().getIssueClosed());
	}

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putCsv_backDatedIssuesOutOfOrder_conflict() throws Exception {
		MockHttpServletRequestBuilder issuePut = putIssues(ApiTests.CSV_CONTENT, "2015-05-15T20:00:00Z")
			.content(CSV_HEADER_SHORT);
		perform(issuePut).andExpect(status().isAccepted());
		issuePut = putIssues(ApiTests.CSV_CONTENT, "2010-05-15T20:00:00Z")
			.content(CSV_HEADER_SHORT);
		perform(issuePut).andExpect(status().isConflict());
	}

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putJson_backDatedIssues_correctDateUsed() throws Exception {
		JSONObject requestCase = new JSONObject();
		String receiptNumber = "FKE31415926";
		String effectiveDate = "2019-12-31T20:00:00Z";
		requestCase.put("receiptNumber", receiptNumber);
		requestCase.put("creationDate", "2001-08-29T00:00:00-04:00");
		requestCase.put("channelType", "Pay Per View");
		MockHttpServletRequestBuilder jsonPut = putIssues(MediaType.APPLICATION_JSON_VALUE, effectiveDate)
				.content("[" + requestCase.toString() + "]");
		perform(jsonPut).andExpect(status().isAccepted());
		CaseIssueUpload lastUpload = _uploadService.getLastUpload(_system, _type, VALID_ISSUE_TYPE).get();
		assertDateEquals("back-dated upload date", effectiveDate, lastUpload.getEffectiveDate());
		CaseDetails details = _detailsService.findCaseDetails(VALID_CASE_MGT_SYS, receiptNumber);
		Optional<? extends CaseIssueSummary> optIssue = details.getIssues().stream().findFirst();
		assertTrue("Issue exists", optIssue.isPresent());
		assertEquals(lastUpload.getEffectiveDate(), optIssue.get().getIssueCreated());
	}

	@Test
	public void getSummary_dataNeverAdded_noLastActive() throws Exception {
		initCaseData();
		_mvc.perform(getSummary(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastUpdated").doesNotExist());
	}


	@Test
	@WithMockUser(authorities = {"READ_CASES", "UPDATE_ISSUES"})
	public void getSummary_emptyCasesAdded_lastActivePresent() throws Exception {
		initCaseData();
		perform(put(ApiTests.API_PATH + "{issueTag}", VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "WONKY")
			.contentType(ApiTests.CSV_CONTENT)
			.with(csrf())
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,2014-08-29T00:00:00-04:00,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
		)).andExpect(status().isAccepted());

		_mvc.perform(getSummary(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastUpdated").isString());
	}

	@Test
	public void search_noCases_emptyResult() throws Exception {
		perform(doSearch(VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "abcde"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true));
	}

	@Test
	public void getCases_smokeTest_emptyResponses() throws Exception {
		perform(doGetCases().param(ApiTests.Filters.MAIN, "ACTIVE"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "ACTIVE")
				.param(ApiTests.Filters.CREATION_START, DATE_STAMP_2018))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "ACTIVE")
				.param(ApiTests.Filters.CREATION_START, DATE_STAMP_2018)
				.param(ApiTests.Filters.CREATION_END, DATE_STAMP_2019))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases().param(ApiTests.Filters.MAIN, "SNOOZED"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "SNOOZED")
				.param(ApiTests.Filters.CREATION_START, DATE_STAMP_2018))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "SNOOZED")
				.param(ApiTests.Filters.CREATION_START, DATE_STAMP_2018)
				.param(ApiTests.Filters.CREATION_END, DATE_STAMP_2019))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "SNOOZED")
				.param(ApiTests.Filters.SNOOZE_REASON, "sleepy"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases().param(ApiTests.Filters.MAIN, "ALARMED"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "ALARMED")
				.param(ApiTests.Filters.CREATION_START, DATE_STAMP_2018))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
		perform(doGetCases()
				.param(ApiTests.Filters.MAIN, "ALARMED")
				.param(ApiTests.Filters.CREATION_START, DATE_STAMP_2018)
				.param(ApiTests.Filters.CREATION_END, DATE_STAMP_2019))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
		;
	}

	/**
	 * Create some data on our default case type!
	 *
	 * 1 case that has 1 issue and is currently active
	 * 1 case that has 1 issue and is currently snoozed
	 */
	private void initCaseData() {
		ZonedDateTime thatWasThen = ZonedDateTime.now().minusMonths(1);
		TroubleCase case1 = _dataService.initCase(_system, "FFFF1111", _type, thatWasThen);
		_dataService.initIssue(case1, "FOOBAR", thatWasThen, null);
		TroubleCase case2 = _dataService.initCase(_system, "FFFF1112", _type, thatWasThen);
		_dataService.initIssue(case2, "FOOBAR", thatWasThen, null);
		_dataService.snoozeCase(case2);
	}

	private void checkUploadRecord(int recordCount, int newIssues, int closedIssues) {
		Optional<CaseIssueUpload> maybeInfo = _uploadService.getLastUpload(_system, _type, VALID_ISSUE_TYPE);
		assertTrue(maybeInfo.isPresent());
		CaseIssueUpload uploadInfo = maybeInfo.get();
		assertEquals(UploadStatus.SUCCESSFUL, uploadInfo.getUploadStatus());
		assertEquals(newIssues, uploadInfo.getNewIssueCount().intValue());
		assertEquals(closedIssues, uploadInfo.getClosedIssueCount().intValue());
		assertEquals(recordCount, uploadInfo.getUploadedRecordCount());
	}

	private static MockHttpServletRequestBuilder getActive(String cmsTag, String ctTag) {
		return get(ApiTests.API_PATH, cmsTag, ctTag).param(ApiTests.Filters.MAIN, "ACTIVE");
	}

	private static MockHttpServletRequestBuilder getSnoozed(String cmsTag, String ctTag) {
		return get(ApiTests.API_PATH, cmsTag, ctTag).param(ApiTests.Filters.MAIN, "SNOOZED");
	}

	private static MockHttpServletRequestBuilder getSummary(String cmsTag, String ctTag) {
		return get(ApiTests.API_PATH + "summary", cmsTag, ctTag);
	}

	private static void assertDateEquals(String message, String expected, ZonedDateTime found) {
		assertDateEquals(message, ZonedDateTime.parse(expected), found);
	}

	private static void assertDateEquals(String message, ZonedDateTime expected, ZonedDateTime found) {
		if (!expected.isEqual(found)) {
			fail(String.format("%s: expected %s, found %s", message, expected, found));
		}
	}
}
