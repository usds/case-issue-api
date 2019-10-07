package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import gov.usds.case_issues.services.CaseListService;

@WithMockUser(authorities = {"READ_CASES", "UPDATE_ISSUES"})
public class CaseApiControllerTest extends ControllerTestBase {

	private static final String API_PATH = "/api/cases/";

	@Autowired
	private CaseListService _caseListService;

	@Before
	public void resetDb() {
		truncateDb();
	}

	@Test
	public void metadata_noCases_empty() throws Exception {
		perform(getMetadata())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastUpdated").isEmpty());
	}

	@Test
	public void metadata_withCases_containsLastUpdated() throws Exception {
		_dataService.ensureCaseTypeInitialized("FOO", "Foo Case", "Where's the foo?");
		_dataService.ensureCaseManagementSystemInitialized("ABC", "Silly system", null);
		_caseListService.putIssueList(
			"ABC", "FOO", "SUPER-OLD", Collections.emptyList(), ZonedDateTime.now()
		);
		perform(getMetadata())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lastUpdated").isString());
	}

	private static MockHttpServletRequestBuilder getMetadata() {
		return get(API_PATH + "metadata");
	}
}
