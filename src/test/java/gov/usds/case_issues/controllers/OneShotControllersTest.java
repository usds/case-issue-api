package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * Consolidated tests for the controllers that have one handler each and are too annoying.
 */
@ActiveProfiles("auth-testing")
public class OneShotControllersTest extends ControllerTestBase {

	@Test
	@WithAnonymousUser
	public void getUser_anonymous_forbidden() throws Exception {
		perform(get("/user")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username="Freddie Fixer", authorities={"RESPECT", "FIGURE"})
	public void getUser_loggedInUser_expectedResult() throws Exception {
		perform(get("/user"))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"name\": \"Freddie Fixer\", \"authorities\":[{\"authority\": \"FIGURE\"},{\"authority\": \"RESPECT\"}]}"))
			;
	}

	@Test
	@WithMockUser(username="Freddie Fixer", authorities={"RESPECT", "FIGURE"})
	public void getUser_loggedInUserOkOrigin_okResult() throws Exception {
		perform(get("/user").header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().isOk())
			;
	}

	@Test
	@WithMockUser(username="Freddie Fixer", authorities={"RESPECT", "FIGURE"})
	public void getUser_loggedInUserBadOrigin_forbidden() throws Exception {
		perform(get("/user").header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden())
			;
	}

	@Test
	@WithAnonymousUser
	public void getCsrf_anonymous_okResult() throws Exception {
		perform(get("/csrf"))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"headerName\": \"X-CSRF-TOKEN\"}")) // matching on the token is not worth it
			;
	}

	@Test
	public void getCsrf_okOrigin_okResult() throws Exception {
		perform(get("/csrf").header("Origin", ORIGIN_HTTP_OK)).andExpect(status().isOk());
	}

	@Test
	public void getCsrf_badOrigin_forbidden() throws Exception {
		perform(get("/csrf").header("Origin", ORIGIN_NOT_OK)).andExpect(status().isForbidden());
	}
}
