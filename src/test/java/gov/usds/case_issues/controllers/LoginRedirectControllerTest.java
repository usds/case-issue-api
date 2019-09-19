package gov.usds.case_issues.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;

import gov.usds.case_issues.config.WebConfigurationProperties;

@ActiveProfiles("mock-properties")
public class LoginRedirectControllerTest extends ControllerTestBase {

	private static final String[] ALLOWED_ORIGINS = {
		"http://example.com",
		"https://nope.org",
		"http://my.friendly.net:8888"
	};

	@Autowired
	private WebConfigurationProperties configProperties;

	@Test
	@WithAnonymousUser
	public void clientLogin_noUser_forbidden() throws Exception {
		// in real life this would redirect to /login, but making the test that realistic is not worth it
		perform(doClientLogin()).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	public void clientLogin_noCors_OK() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(null);
		perform(doClientLogin())
			.andExpect(status().isOk())
			.andExpect(content().string(Matchers.containsString("Login successful")))
			;
	}

	@Test
	@WithMockUser
	public void clientLogin_corsNoRedirect_defaultRedirect() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		perform(doClientLogin())
			.andExpect(status().isFound())
			.andExpect(header().string("Location", "http://example.com"))
		;
	}

	@Test
	@WithMockUser
	public void clientLogin_corsWithSimpleRedirect_defaultRedirect() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		perform(doClientLogin("https://nope.org"))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", "https://nope.org"))
		;
	}

	@Test
	@WithMockUser
	public void clientLogin_corsWithPathInRedirect_redirected() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		String redirect = "https://nope.org/landingPage";
		perform(doClientLogin(redirect))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", redirect))
		;
	}

	@Test
	@WithMockUser
	public void clientLogin_corsWithPortInRedirect_redirected() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		String redirect = "http://my.friendly.net:8888";
		perform(doClientLogin(redirect))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", redirect))
		;
	}

	@Test
	@WithMockUser
	public void clientLogin_corsWithPortAndPathInRedirect_redirected() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		String redirect = "http://my.friendly.net:8888/yo?src=dawg";
		perform(doClientLogin(redirect))
			.andExpect(status().isFound())
			.andExpect(header().string("Location", redirect))
		;
	}

	@Test(expected=NestedServletException.class) // needs a ControllerAdvice fix to wrap nicely
	@WithMockUser
	public void clientLogin_corsWithEvilPort_error() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		perform(doClientLogin("https://nope.org:8000"))
			.andExpect(status().isBadRequest())
		;
	}

	@Test(expected=NestedServletException.class)
	@WithMockUser
	public void clientLogin_corsWithEvilUserinfo_error() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		perform(doClientLogin("https://nope.org@other.site.com/"))
			.andExpect(status().isBadRequest())
		;
	}

	@Test(expected=NestedServletException.class)
	@WithMockUser
	public void clientLogin_nonUrlRedirect_error() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		perform(doClientLogin("howdy"))
			.andExpect(status().isBadRequest())
		;
	}

	@Test(expected=NestedServletException.class)
	@WithMockUser
	public void clientLogin_nonAllowedRedirect_error() throws Exception {
		when(configProperties.getCorsOrigins()).thenReturn(ALLOWED_ORIGINS);
		perform(doClientLogin("http://www.whitehouse.gov"))
			.andExpect(status().isBadRequest())
		;
	}

	private MockHttpServletRequestBuilder doClientLogin(String... redirects) {
		MockHttpServletRequestBuilder request = get("/clientLogin").accept("text/html");
		if (redirects != null && redirects.length != 0) {
			if (redirects.length > 1) {
				throw new IllegalArgumentException("No stress testing the test harness");
			}
			request.param("redirect", redirects);
		}
		return request;
	}

}
