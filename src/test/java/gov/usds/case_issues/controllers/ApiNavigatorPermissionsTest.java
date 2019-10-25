package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
		browserGet("/browser/index.html").andExpect(status().isOk());
	}

	@Test
	public void getHalResource_anonymous_ok() throws Exception {
		browserGet("/browser/vendor/img/ajax-loader.gif").andExpect(status().isOk());
	}

	@Test
	public void getSwaggerUi_anonymous_ok() throws Exception {
		perform(get("/swagger-ui.html")).andExpect(status().isOk());
	}

	@Test
	public void getSwaggerApi_anonymous_ok() throws Exception {
		perform(get("/v2/api-docs")).andExpect(status().isOk());
	}
}
