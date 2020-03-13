package gov.usds.case_issues.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.hamcrest.Matchers;
import org.junit.Test;

import gov.usds.case_issues.controllers.ControllerTestBase;

/**
 * Tests for the behavior of the application when presented with an x509 certificate as user authentication.
 *
 */
public class X509UserTest extends ControllerTestBase {

	@Test
	public void getUserInformation_withNormalUser_userInfoFound() throws Exception {
		perform(get("/api/users").with(x509("normal-user-name.crt")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("normal user name"))
			.andExpect(jsonPath("$.id").value(Matchers.startsWith("UID=be3bde871002e9fc83b0a387f485f363, CN=normal user name")))
			.andExpect(jsonPath("$.id").value(Matchers.endsWith("C=US")))
			;
	}

	@Test
	public void getUserInformation_withLongUserDn_userInfoFound() throws Exception { // this user has a DN longer than 100 but under 255 characters
		perform(get("/api/users").with(x509("medium-long-user-name.crt")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("medium-long-username"))
			.andExpect(jsonPath("$.id").value(Matchers.startsWith("UID=0bc6c46ca38ef3e8af3e50bc7ac244f7, CN=medium-long-username")))
			.andExpect(jsonPath("$.id").value(Matchers.endsWith("C=US")))
			;
	}

	@Test(expected=IllegalArgumentException.class)
	public void getUserInformation_withExtremeUserDn_serverError() throws Exception { // this user has a DN longer than 256 characters
		perform(get("/api/users").with(x509("extra-long-user-name.crt")))
			;
	}
}
