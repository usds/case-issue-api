package gov.usds.case_issues.controllers;

import static gov.usds.case_issues.controllers.ApiTests.doGetCases;
import static gov.usds.case_issues.controllers.ApiTests.doSearch;
import static gov.usds.case_issues.controllers.ApiTests.putIssues;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.config.DataFormatSpec;
import gov.usds.case_issues.config.WebConfigurationProperties;
import gov.usds.case_issues.controllers.ApiTests.Filters;
import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.services.CaseFilteringService;
import gov.usds.case_issues.services.CaseListService;
import gov.usds.case_issues.services.IssueUploadService;
/**
 * Tests of the API controller that don't rely on the behavior of the underlying data store.
 * (Argument validation, security configuration.)
 */
@RunWith(SpringRunner.class)
@WebMvcTest(HitlistApiController.class)
@WithMockUser(username = "default_hitlist_user", authorities = "READ_CASES")
public class HitlistApiControllerTest {

	private static final String DUPE_INPUT_ERROR_MESSAGE = "{\"message\":\"Multiple records in input with same receipt number (DUPE123)\"}";
	private static final String NO_OP = "this request intentionally left blank";
	private static final String DATE_STAMP_2018 = "2018-01-01T12:00:00Z";
	private static final String DATE_STAMP_2019 = "2019-01-01T12:00:00Z";

	@MockBean
	private IssueUploadService _uploadService;
	@MockBean
	private CaseListService _listService;
	@MockBean
	private CaseFilteringService _filterService;
	@MockBean
	private WebConfigurationProperties _properties;

	@Autowired
	private MockMvc _mvc;

	@Test
	public void search_withoutQueryParam_badRequest() throws Exception {
		perform(doSearch(ApiTests.VALID_CASE_MGT_SYS, ApiTests.VALID_CASE_TYPE, null))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(""));
	}

	@Test
	public void search_invalidInput_badRequest() throws Exception {
		perform(doSearch(ApiTests.VALID_CASE_MGT_SYS, ApiTests.VALID_CASE_TYPE, "ab cde"))
			.andExpect(status().isBadRequest())
		;
		perform(doSearch(ApiTests.VALID_CASE_MGT_SYS, ApiTests.VALID_CASE_TYPE, "ab\ncde"))
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
			.andExpect(status().isBadRequest())
			.andExpect(content().json("{\"message\": \"Range end must be after beginning\"}"))
			;
		perform(doGetCases()
				.param(Filters.CREATION_START, DATE_STAMP_2019)
				.param(Filters.CREATION_END, "2021-01-01T12:00:00Z")
			   )
			.andExpect(status().isBadRequest())
			// it would be nice if we controlled the error output here, but we don't
			;
	}

	@Test
	public void getCases_badFilterCombination_badRequest() throws Exception {
		String expectedErrorJson =
			"{\"message\": \"Snooze reason cannot be specified for cases that are not snoozed\"}";
		perform(doGetCases()
				.param(Filters.MAIN, "ACTIVE")
				.param(Filters.SNOOZE_REASON, "anything")
			   )
			.andExpect(status().isBadRequest())
			.andExpect(content().json(expectedErrorJson))
			;
		perform(doGetCases()
				.param(Filters.MAIN, "ALARMED")
				.param(Filters.SNOOZE_REASON, "anything")
			   )
			.andExpect(status().isBadRequest())
			.andExpect(content().json(expectedErrorJson))
			;
		perform(doGetCases()
				.param(Filters.MAIN, "UNCHECKED")
				.param(Filters.SNOOZE_REASON, "anything")
			   )
			.andExpect(status().isBadRequest())
			.andExpect(content().json(expectedErrorJson))
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

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putJson_badUploadStatus_serverError() throws Exception {
		Mockito.when(_uploadService.putIssueList(ArgumentMatchers.any(), ArgumentMatchers.eq(ApiTests.VALID_ISSUE_TYPE), ArgumentMatchers.any(), ArgumentMatchers.any()))
			.thenReturn(makeStatus(UploadStatus.FAILED));
		_mvc.perform(putIssues(MediaType.APPLICATION_JSON_VALUE).content("[]"))
			.andExpect(status().isInternalServerError());
		_mvc.perform(putIssues(MediaType.APPLICATION_JSON_VALUE, "2001-01-01T00:00:00Z").content("[]"))
			.andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putCsv_badUploadStatus_serverError() throws Exception {
		Mockito.when(_uploadService.putIssueList(ArgumentMatchers.any(), ArgumentMatchers.eq(ApiTests.VALID_ISSUE_TYPE), ArgumentMatchers.any(), ArgumentMatchers.any()))
			.thenReturn(makeStatus(UploadStatus.FAILED));
		String csvHeader = "receiptNumber,creationDate,caseStatus";
		_mvc.perform(putIssues(ApiTests.CSV_CONTENT).content(csvHeader))
			.andExpect(status().isInternalServerError());
		_mvc.perform(putIssues(ApiTests.CSV_CONTENT, "2001-01-01T00:00:00Z").content(csvHeader))
			.andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putJson_emptyListNoCsrf_forbidden() throws Exception {
		MockHttpServletRequestBuilder jsonPut =
			put(ApiTests.ISSUE_UPLOAD_PATH, ApiTests.VALID_CASE_MGT_SYS, ApiTests.VALID_CASE_TYPE, ApiTests.VALID_ISSUE_TYPE)
			.contentType(MediaType.APPLICATION_JSON)
 			.content("[]");
		_mvc.perform(jsonPut).andExpect(status().isForbidden());
		Mockito.verifyNoMoreInteractions(_listService, _uploadService);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putJson_backDatedWithoutStructureAuthority_forbidden() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues(MediaType.APPLICATION_JSON_VALUE, "2019-12-31T20:00:00Z")
				.content("[]");
		_mvc.perform(jsonPut).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_STRUCTURE")
	public void putJson_backDatedWithoutIssuesAuthority_forbidden() throws Exception {
		MockHttpServletRequestBuilder jsonPut = putIssues(MediaType.APPLICATION_JSON_VALUE, "2019-12-31T20:00:00Z")
			.content("[]");
		_mvc.perform(jsonPut).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putJson_duplicateRows_badRequest() throws Exception {
		Mockito.when(_listService.getUploadFormat(ArgumentMatchers.isNull()))
			.thenReturn(new DataFormatSpec());
		String dupeArray = "["
			+ "{\"receiptNumber\": \"DUPE123\", \"creationDate\": \"2001-01-01T01:02:03Z\"},"
			+ "{\"receiptNumber\": \"DUPE123\", \"creationDate\": \"2001-01-01T01:02:04Z\"}"
			+ "]";
		MockHttpServletRequestBuilder jsonPut = putIssues(MediaType.APPLICATION_JSON_VALUE)
				.content(dupeArray);
		_mvc.perform(jsonPut)
			.andExpect(status().isBadRequest())
			.andExpect(content().json(DUPE_INPUT_ERROR_MESSAGE))
			;
		jsonPut = putIssues(MediaType.APPLICATION_JSON_VALUE, "2019-12-31T20:00:00Z")
			.content(dupeArray);
		_mvc.perform(jsonPut)
			.andExpect(status().isBadRequest())
			.andExpect(content().json(DUPE_INPUT_ERROR_MESSAGE))
			;
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_invalidCreationDate_badRequest() throws Exception {
		Mockito.when(_listService.getUploadFormat(ArgumentMatchers.isNull()))
			.thenReturn(new DataFormatSpec());
		MockHttpServletRequestBuilder jsonPut = putIssues(ApiTests.CSV_CONTENT)
			.content(
				"receiptNumber,creationDate,caseAge,channelType,caseState,i90SP,caseStatus,applicationReason,caseId,caseSubstatus\n" +
				"FKE5250608,NOT A DATE,1816,Pigeon,Happy,true,Eschewing Obfuscation,Boredom,43375,Scrutinizing\n"
			);
		_mvc.perform(jsonPut).andExpect(status().isBadRequest());
		Mockito.verifyNoMoreInteractions(_uploadService);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_ISSUES")
	public void putCsv_backDatedWithoutStructureAuthority_forbidden() throws Exception {
		MockHttpServletRequestBuilder issuePut = putIssues(ApiTests.CSV_CONTENT, "2019-12-31T20:00:00Z")
			.content(NO_OP);
		_mvc.perform(issuePut).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = "UPDATE_STRUCTURE")
	public void putCsv_backDatedWithoutIssuesAuthority_forbidden() throws Exception {
		MockHttpServletRequestBuilder issuePut = putIssues(ApiTests.CSV_CONTENT, "2019-12-31T20:00:00Z")
			.content(NO_OP);
		_mvc.perform(issuePut).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities = {"UPDATE_ISSUES", "UPDATE_STRUCTURE"})
	public void putCsv_duplicateRows_badRequest() throws Exception {
		Mockito.when(_listService.getUploadFormat(ArgumentMatchers.isNull()))
			.thenReturn(new DataFormatSpec());
		String csvContent = "receiptNumber,creationDate,whosit,\n"
				+ "DUPE123,1978-08-05T00:00:00Z,True\n"
				+ "DUPE123,1978-08-05T00:00:00Z,False\n";
		MockHttpServletRequestBuilder issuePut = putIssues(ApiTests.CSV_CONTENT)
			.content(csvContent);
		_mvc.perform(issuePut)
			.andExpect(status().isBadRequest())
			.andExpect(content().json(DUPE_INPUT_ERROR_MESSAGE))
			;
		issuePut = putIssues(ApiTests.CSV_CONTENT, "2019-12-31T20:00:00Z")
			.content(csvContent);
		_mvc.perform(issuePut)
			.andExpect(status().isBadRequest())
			.andExpect(content().json(DUPE_INPUT_ERROR_MESSAGE))
			;
	}

	private static CaseIssueUpload makeStatus(UploadStatus wanted) {
		CaseIssueUpload upload = new CaseIssueUpload(null, null, "DUMMY1", null, 0);
		upload.setUploadStatus(wanted);
		return upload;
	}

	// inline this eventually probably
	private ResultActions perform(MockHttpServletRequestBuilder req) throws Exception {
		return _mvc.perform(req);
	}

}
