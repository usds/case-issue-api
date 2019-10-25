package gov.usds.case_issues.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@AutoConfigureMockMvc
@WithMockUser
public abstract class ControllerTestBase extends CaseIssueApiTestBase {

	protected static final String ORIGIN_HTTPS_OK = "https://ok-client.gov";
	protected static final String ORIGIN_HTTP_OK = "http://ok-client.net";
	protected static final String ORIGIN_NOT_OK = "http://evil-client.com";

	@Autowired
	protected MockMvc _mvc;

	protected ResultActions perform(RequestBuilder requestBuilder) throws Exception {
		return _mvc.perform(requestBuilder);
	}
}
