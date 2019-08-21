package gov.usds.case_issues.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@AutoConfigureMockMvc
public abstract class ControllerTestBase extends CaseIssueApiTestBase {

	@Autowired
	protected MockMvc _mvc;

	protected ResultActions perform(RequestBuilder requestBuilder) throws Exception {
		return _mvc.perform(requestBuilder);
	}
}
