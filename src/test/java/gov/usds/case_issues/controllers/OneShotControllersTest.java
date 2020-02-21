package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserInformationRepository;

/**
 * Consolidated tests for the controllers that have one handler each and are too annoying.
 */
@ActiveProfiles("auth-testing")
public class OneShotControllersTest extends ControllerTestBase {

	@Autowired
	private UserInformationRepository _userRepo;

	@Before
	public void resetDb() {
		truncateDb();
	}

	private static MockHttpServletRequestBuilder getUser() {
		return get(UserInformationApiController.USER_INFO_ENDPOINT);
	}

	private static MockHttpServletRequestBuilder getCsrf() {
		return get("/csrf");
	}

	@Test
	@WithAnonymousUser
	public void getUser_anonymous_forbidden() throws Exception {
		perform(getUser()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username="Freddie Fixer", authorities={"RESPECT", "FIGURE"})
	public void getUser_loggedInUser_expectedResult() throws Exception {
		createMockDatabaseUser("Freddie Fixer");
		perform(getUser())
			.andExpect(status().isOk())
			.andExpect(content().json("{\"name\": \"Freddie Fixer\"}"))
			;
	}

	@Test
	@WithMockUser(username="Freddie Fixer", authorities={"RESPECT", "FIGURE"})
	public void getUser_loggedInUserOkOrigin_okResult() throws Exception {
		createMockDatabaseUser("Freddie Fixer");
		perform(getUser().header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().isOk())
			;
	}

	@Test
	@WithMockUser(username="Freddie Fixer", authorities={"RESPECT", "FIGURE"})
	public void getUser_loggedInUserBadOrigin_forbidden() throws Exception {
		perform(getUser().header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden())
			;
	}

	@Test
	@WithAnonymousUser
	// we allow this because it's convenient for users of Swagger-UI in the dev environment
	// it is not clear that this is sufficent justification, though also it's not clear that
	// it matters much (could spam this URL to create a lot of dead sessions, I guess?)
	public void getCsrf_anonymous_okResult() throws Exception {
		perform(getCsrf()).andExpect(status().isOk());
	}

	@Test
	public void getCsrf_okOrigin_okResult() throws Exception {
		perform(getCsrf().header("Origin", ORIGIN_HTTP_OK))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"headerName\": \"X-CSRF-TOKEN\"}")) // matching on the token is not worth it
			;
	}

	@Test
	public void getCsrf_badOrigin_forbidden() throws Exception {
		perform(getCsrf().header("Origin", ORIGIN_NOT_OK)).andExpect(status().isForbidden());
	}

	@Test
	@WithAnonymousUser
	public void getHealth_anonymous_okResult() throws Exception {
		perform(get("/health")).andExpect(status().isOk()).andExpect(content().string(""));
	}

	@Test
	@WithMockUser
	public void getHealth_loggedInUser_okResult() throws Exception {
		perform(get("/health")).andExpect(status().isOk()).andExpect(content().string(""));
	}

	private void createMockDatabaseUser(String name) {
		UserInformation user = new UserInformation(name, name);
		_userRepo.save(user);
	}
}
