package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.ResultActions;

@WithAnonymousUser
public class ApiNavigatorPermissionsTest extends ControllerTestBase {

	@Value("${spring.data.rest.basePath}")
	private String _browserPath;

	protected ResultActions browserGet(String relativePath) throws Exception {
		return perform(get(_browserPath + relativePath));
	}

	@Test
	public void getHalBrowser_anonymous_ok() throws Exception {
		browserGet("").andExpect(status().isOk());
	}

	@Test
	public void getHalIndex_anonymous_ok() throws Exception {
		browserGet("/explorer/index.html").andExpect(status().isOk());
	}

	@Test
	public void getHalResource_anonymous_ok() throws Exception {
		browserGet("/explorer/favicon.ico").andExpect(status().isOk());
	}

	@Test
	public void getFavicon_anonymous_notFound() throws Exception {
		perform(get("/favicon.ico")).andExpect(status().isNotFound()); // there is no default, but it's allowed to check
	}

	@Test
	public void getSwaggerUiLegacy_anonymous_found() throws Exception {
		perform(get("/swagger-ui.html"))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/swagger-ui/"))
		;
	}

	@Test
	public void getSwaggerUi_anonymous_ok() throws Exception {
		perform(get("/swagger-ui/")).andExpect(status().isOk());
	}
	@Test
	public void getSwaggerApi_anonymous_ok() throws Exception {
		perform(get("/v2/api-docs")).andExpect(status().isOk());
	}
}
