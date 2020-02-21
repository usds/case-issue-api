package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
public class SystemNavigationControllerTest extends ControllerTestBase {

	@Before
	public void resetDb() {
		truncateDb();
	}

	@Test
	public void getNavigationInformation_noData_emptyList() throws Exception {
		perform(get("/api/navigation"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]", true))
			;
	}

	@Test
	public void getNavigationInformation_trivialData_singleItemList() throws Exception {
		_dataService.ensureCaseManagementSystemInitialized("YO", "Your case manager");
		_dataService.ensureCaseTypeInitialized("W2", "Income Reporting", "That form you get every January");
		perform(get("/api/navigation"))
			.andExpect(status().isOk())
			.andExpect(content().json("[{\"tag\": \"YO\", \"name\": \"Your case manager\", "
					+ "\"caseTypes\": [{\"tag\":\"W2\", \"name\": \"Income Reporting\","
					+ " \"description\": \"That form you get every January\"}]}]"))
			;
	}

	@Test
	public void getNavigationInformation_okOrigin_resultsFound() throws Exception {
		_dataService.ensureCaseManagementSystemInitialized("YO", "Your case manager");
		_dataService.ensureCaseTypeInitialized("W2", "Income Reporting", "That form you get every January");
		perform(get("/api/navigation").header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().isOk())
			.andExpect(content().json("[{\"tag\": \"YO\", \"name\": \"Your case manager\", "
					+ "\"caseTypes\": [{\"tag\":\"W2\", \"name\": \"Income Reporting\","
					+ " \"description\": \"That form you get every January\"}]}]"))
			;

	}

	@Test
	public void getNavigationInformation_badOrigin_forbidden() throws Exception {
		perform(get("/api/navigation").header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden())
			;
	}
}
