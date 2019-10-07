package gov.usds.case_issues.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

@WithMockUser(username = "default_hitlist_user", authorities = "READ_CASES")
public class HitlistApiControllerTest extends ControllerTestBase {

	private static final String VALID_CASE_TYPE = "C1";
	private static final String VALID_CASE_MGT_SYS = "F1";
	private static final String API_PATH = "/api/cases/{caseManagementSystemTag}/{caseTypeTag}/";
	private static final String CASE_TYPE_NOPE = "Case Type 'NOPE' was not found";
	private static final String CASE_MANAGEMENT_SYSTEM_NOPE = "Case Management System 'NOPE' was not found";

	private CaseManagementSystem _system;
	private CaseType _type;

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
		MockHttpServletRequestBuilder jsonPut = put(API_PATH + "{issueTag}", VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "WONKY")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf())
			.content("[]");
		perform(jsonPut).andExpect(status().isAccepted());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putJson_emptyListNoCsrf_forbidden() throws Exception {
		MockHttpServletRequestBuilder jsonPut = put(API_PATH + "{issueTag}", VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "WONKY")
			.contentType(MediaType.APPLICATION_JSON)
			.content("[]");
		perform(jsonPut).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_emptyList_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = put(API_PATH + "{issueTag}", VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "WONKY")
			.contentType("text/csv")
			.with(csrf())
			.content("header1,header2\n");
		perform(jsonPut).andExpect(status().isAccepted());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_singleCase_accepted() throws Exception {
		MockHttpServletRequestBuilder jsonPut = put(API_PATH + "{issueTag}", VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "WONKY")
			.contentType("text/csv")
			.with(csrf())
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,2014-08-29T00:00:00-04:00,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
			);
		perform(jsonPut).andExpect(status().isAccepted());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_invalidCreationDate_badRequest() throws Exception {
		MockHttpServletRequestBuilder jsonPut = put("/api/cases/{caseManagementSystemTag}/{caseTypeTag}/{issueTag}", VALID_CASE_MGT_SYS, VALID_CASE_TYPE, "WONKY")
			.contentType("text/csv")
			.with(csrf())
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,NOT A DATE,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
			);
		perform(jsonPut).andExpect(status().isBadRequest());
	}

	@Test
	public void search_withoutQueryParam_badRequest() throws Exception {
		perform(doSearch(VALID_CASE_MGT_SYS, VALID_CASE_TYPE, null))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(""));
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

}
