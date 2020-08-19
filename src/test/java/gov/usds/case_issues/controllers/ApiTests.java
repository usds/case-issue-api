package gov.usds.case_issues.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Container for shared HTTP test constants.
 */
public final class ApiTests {

	public static class Filters {
		static final String MAIN = "mainFilter";
		static final String CREATION_START = "caseCreationRangeBegin";
		static final String CREATION_END = "caseCreationRangeEnd";
		static final String SNOOZE_REASON = "snoozeReason";
	}

	public static final class FilterParams {
		public static final String STEM = "filter_";
		public static final String LINK_BY_TYPE = "hasLinkType";
		public static final String LINK_BY_CONTENT = "hasLink";
		public static final String TAG_BY_TYPE = "hasTagType";
		public static final String TAG_BY_CONTENT = "hasTag";
		public static final String COMMENT_ANY = "hasAnyComment";
		public static final String COMMENT_CONTENT = "hasComment";
		public static final String DATA_FIELD = "dataField";
	}

	public static final String API_PATH = "/api/cases/{caseManagementSystemTag}/{caseTypeTag}/";
	public static final String ISSUE_UPLOAD_PATH = API_PATH + "{issueTag}";
	public static final String CSV_CONTENT = "text/csv";

	public static final String VALID_CASE_MGT_SYS = "F1";
	public static final String VALID_CASE_TYPE = "C1";
	public static final String VALID_ISSUE_TYPE = "WONKY";

	static MockHttpServletRequestBuilder doGetCases() {
		return get(API_PATH, VALID_CASE_MGT_SYS, VALID_CASE_TYPE);
	}

	static MockHttpServletRequestBuilder doSearch(String cmsTag, String ctTag, String queryString) {
		return get(API_PATH + "search", cmsTag, ctTag).param("query", queryString);
	}

	public static MockHttpServletRequestBuilder putIssues(String contentType, String effectiveDate) {
		return putIssues(contentType).param("effectiveDate", effectiveDate);
	}

	public static MockHttpServletRequestBuilder putIssues(String contentType) {
		return put(ISSUE_UPLOAD_PATH, VALID_CASE_MGT_SYS, VALID_CASE_TYPE, VALID_ISSUE_TYPE)
			.contentType(contentType)
			.with(csrf());
	}
}
