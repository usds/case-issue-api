package gov.usds.case_issues.controllers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Tests to validate that our cookie configuration is working the way we think it is.
 */
public class SessionCookieTest extends ControllerTestBase {

	private static final String SESSION_COOKIE = "SESSION";

	@Test
	public void vanillaRequest_expectedFlags() throws Exception {
		perform(vanillaGet())
			.andExpect(cookie().exists(SESSION_COOKIE))
			.andExpect(cookie().secure(SESSION_COOKIE, false))
			.andExpect(cookie().httpOnly(SESSION_COOKIE, true))
			.andExpect(header().string("Set-Cookie", containsString("SameSite=None")))
		;
	}

	@Test
	public void forwardingHeadersUsed_secureSetCorrectly() throws Exception {
		perform(vanillaGet().header("X-Forwarded-Proto", "https"))
			.andExpect(cookie().exists(SESSION_COOKIE))
			.andExpect(cookie().secure(SESSION_COOKIE, true))
		;
		perform(vanillaGet().header("Forwarded", "host=example.com;proto=https"))
			.andExpect(cookie().exists(SESSION_COOKIE))
			.andExpect(cookie().secure(SESSION_COOKIE, true))
		;
		perform(vanillaGet().header("X-Forwarded-Proto", "http"))
			.andExpect(cookie().exists(SESSION_COOKIE))
			.andExpect(cookie().secure(SESSION_COOKIE, false))
		;
		perform(vanillaGet().header("Forwarded", "host=example.com;proto=http"))
			.andExpect(cookie().exists(SESSION_COOKIE))
			.andExpect(cookie().secure(SESSION_COOKIE, false))
		;
	}

	/** Perform a GET request to a URL that will set a session cookie without our needing to log in */
	private MockHttpServletRequestBuilder vanillaGet() {
		return get("/csrf");
	}
}
