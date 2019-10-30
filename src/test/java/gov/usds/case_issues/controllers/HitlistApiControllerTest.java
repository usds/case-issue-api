package gov.usds.case_issues.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

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
import gov.usds.case_issues.services.UploadStatusService;

@WithMockUser(username = "default_hitlist_user", authorities = "READ_CASES")
public class HitlistApiControllerTest extends ControllerTestBase {

	private static final String VALUE_ISSUE_TYPE = "WONKY";
	private static final String VALID_CASE_TYPE = "C1";
	private static final String VALID_CASE_MGT_SYS = "F1";
	private static final String API_PATH = "/api/cases/{caseManagementSystemTag}/{caseTypeTag}/";
	private static final String ISSUE_UPLOAD_PATH = API_PATH + "{issueTag}";
	private static final String CASE_TYPE_NOPE = "Case Type 'NOPE' was not found";
	private static final String CASE_MANAGEMENT_SYSTEM_NOPE = "Case Management System 'NOPE' was not found";

	private CaseManagementSystem _system;
	private CaseType _type;

	@Autowired
	private UploadStatusService _uploadService;

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
			.andExpect(content().json("{}", true))
		;
	}

	@Test
	public void getActive_withData_correctResponse() throws Exception {
		initCaseData();
		_mvc.perform(getSummary(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("{'NEVER_SNOOZED': 1, 'CURRENTLY_SNOOZED': 1}", true))
		;
		_mvc.perform(getActive(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("[{'receiptNumber': 'FFFF1111', 'previouslySnoozed': false}]", false))
		;
		_mvc.perform(getSnoozed(VALID_CASE_MGT_SYS, VALID_CASE_TYPE))
			.andExpect(status().isOk())
			.andExpect(content().json("[{'receiptNumber': 'FFFF1112', 'snoozeInformation': {'snoozeReason': 'DONOTCARE'}}]", false))
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
	public void putJson_emptyListNoCsrf_forbidden() throws Exception {
		MockHttpServletRequestBuilder jsonPut = put(ISSUE_UPLOAD_PATH, VALID_CASE_MGT_SYS, VALID_CASE_TYPE, VALUE_ISSUE_TYPE)
			.contentType(MediaType.APPLICATION_JSON)
			.content("[]");
		perform(jsonPut).andExpect(status().isForbidden());
		assertEquals("No upload records should exist",
				0, _uploadService.getUploadHistory(_system, _type).size());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_emptyList_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues("text/csv")
			.content("header1,header2\n");
		perform(jsonPut).andExpect(status().isAccepted());
		checkUploadRecord(0, 0, 0);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_singleCase_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues("text/csv")
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,2014-08-29T00:00:00-04:00,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
			);
		perform(jsonPut).andExpect(status().isAccepted());
		checkUploadRecord(1, 1, 0);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_invalidCreationDate_badRequest() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues("text/csv")
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,NOT A DATE,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
			);
		perform(jsonPut).andExpect(status().isBadRequest());
		assertEquals("No upload records should exist",
			0, _uploadService.getUploadHistory(_system, _type).size());
	}

	@Test
	public void search_noCases_emptyResult() throws Exception {
		perform(doSearch(VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "abcde"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true));
	}

	@Test
	public void search_invalidInput_badRequest() throws Exception {
		perform(doSearch(VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "ab cde"))
			.andExpect(status().isBadRequest())
		;
		perform(doSearch(VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "ab\ncde"))
			.andExpect(status().isBadRequest())
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
		CaseIssueUpload uploadInfo = _uploadService.getLastUpload(_system, _type, VALUE_ISSUE_TYPE);
		assertNotNull(uploadInfo);
		assertEquals(UploadStatus.SUCCESSFUL, uploadInfo.getUploadStatus());
		assertEquals(newIssues, uploadInfo.getNewIssueCount().intValue());
		assertEquals(closedIssues, uploadInfo.getClosedIssueCount().intValue());
		assertEquals(recordCount, uploadInfo.getUploadedRecordCount());
	}

	private static MockHttpServletRequestBuilder doSearch(String cmsTag, String ctTag, String queryString) {
		return get(API_PATH + "search", cmsTag, ctTag).param("query", queryString);
	}
	private static MockHttpServletRequestBuilder getActive(String cmsTag, String ctTag) {
		return get(API_PATH + "active", cmsTag, ctTag);
	}

	private static MockHttpServletRequestBuilder getSnoozed(String cmsTag, String ctTag) {
		return get(API_PATH + "snoozed", cmsTag, ctTag);
	}

	private static MockHttpServletRequestBuilder getSummary(String cmsTag, String ctTag) {
		return get(API_PATH + "summary", cmsTag, ctTag);
	}

	private static MockHttpServletRequestBuilder putIssues(String contentType) {
		return put(ISSUE_UPLOAD_PATH, VALID_CASE_MGT_SYS, VALID_CASE_TYPE, VALUE_ISSUE_TYPE)
			.contentType(contentType)
			.with(csrf());
	}

}
