package gov.usds.case_issues.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.services.UploadStatusService;

@WithMockUser(username = "default_hitlist_user", authorities = "READ_CASES")
public class HitlistApiControllerTest extends ControllerTestBase {

	private static final String DATE_STAMP_2018 = "2018-01-01T12:00:00Z";
	private static final String DATE_STAMP_2019 = "2019-01-01T12:00:00Z";
	private static final String VALUE_ISSUE_TYPE = "WONKY";
	private static final String VALID_CASE_TYPE = "C1";
	private static final String VALID_CASE_MGT_SYS = "F1";
	protected static final String API_PATH = "/api/cases/{caseManagementSystemTag}/{caseTypeTag}/";
	private static final String ISSUE_UPLOAD_PATH = API_PATH + "{issueTag}";

	private static class Filters {
		private static final String MAIN = "mainFilter";
		private static final String CREATION_START = "caseCreationRangeBegin";
		private static final String CREATION_END = "caseCreationRangeEnd";
	}

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

	@Test
	public void getCases_badFilterArguments_badRequest() throws Exception {
		perform(doGetCases())
			.andExpect(status().isBadRequest());
		perform(doGetCases().param(Filters.MAIN, "FAKE"))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void getCases_badDateRangeArguments_badRequest() throws Exception {
		perform(doGetCases().param(Filters.CREATION_START, "3/5/1995"))
			.andExpect(status().isBadRequest())
			;
		perform(doGetCases()
				.param(Filters.MAIN, "ACTIVE") // has to get past initial required-argument filter
				.param(Filters.CREATION_START, DATE_STAMP_2019)
				.param(Filters.CREATION_END, DATE_STAMP_2018)
			   )
			.andExpect(status().isBadRequest());
		perform(doGetCases()
				.param(Filters.CREATION_START, DATE_STAMP_2019)
				.param(Filters.CREATION_END, "2021-01-01T12:00:00Z")
			   )
			.andExpect(status().isBadRequest())
			// it would be nice if we controlled the error output here, but we don't
			;
	}

	@Test
	public void getCases_smokeTest_emptyResponses() throws Exception {
		perform(doGetCases().param(Filters.MAIN, "ALARMED"))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"cases\": [], \"totalCount\": 0, \"queryCount\": 0}", true))
		;
		perform(doGetCases()
				.param(Filters.MAIN, "ALARMED")
				.param(Filters.CREATION_START, DATE_STAMP_2018))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"cases\": [], \"totalCount\": 0, \"queryCount\": 0}", true))
		;
		perform(doGetCases()
				.param(Filters.MAIN, "ALARMED")
				.param(Filters.CREATION_START, DATE_STAMP_2018)
				.param(Filters.CREATION_END, DATE_STAMP_2019))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"cases\": [], \"totalCount\": 0, \"queryCount\": 0}", true))
		;
	}

	@Test
	public void getCases_invalidAdditionalParamNames_badRequest() throws Exception {
		perform(doGetCases().param(Filters.MAIN, "ALARMED").param("+nope", "1", "2", "3"))
			.andExpect(status().isBadRequest())
		;
		perform(doGetCases().param(Filters.MAIN, "ALARMED").param("-flag", "true"))
			.andExpect(status().isBadRequest())
		;
		perform(doGetCases().param(Filters.MAIN, "ALARMED").param("inject\nme", "plzkthx"))
			.andExpect(status().isBadRequest())
		;
		perform(doGetCases().param(Filters.MAIN, "ALARMED").param("filter on awesomeness", "true"))
			.andExpect(status().isBadRequest())
		;
	}

	private void checkUploadRecord(int recordCount, int newIssues, int closedIssues) {
		CaseIssueUpload uploadInfo = _uploadService.getLastUpload(_system, _type, VALUE_ISSUE_TYPE);
		assertNotNull(uploadInfo);
		assertEquals(UploadStatus.SUCCESSFUL, uploadInfo.getUploadStatus());
		assertEquals(newIssues, uploadInfo.getNewIssueCount().intValue());
		assertEquals(closedIssues, uploadInfo.getClosedIssueCount().intValue());
		assertEquals(recordCount, uploadInfo.getUploadedRecordCount());
	}

	private static MockHttpServletRequestBuilder doGetCases() {
		return get(API_PATH, VALID_CASE_MGT_SYS, VALID_CASE_TYPE);
	}

	private static MockHttpServletRequestBuilder doSearch(String cmsTag, String ctTag, String queryString) {
		return get(API_PATH + "search", cmsTag, ctTag).param("query", queryString);
	}

	private static MockHttpServletRequestBuilder putIssues(String contentType) {
		return put(ISSUE_UPLOAD_PATH, VALID_CASE_MGT_SYS, VALID_CASE_TYPE, VALUE_ISSUE_TYPE)
			.contentType(contentType)
			.with(csrf());
	}

}
