package gov.usds.case_issues;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestDataRoundtripTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private RepositoryEntityLinks links;
	@Value("${spring.data.rest.basePath}") // is this terrible?
	private String basePath;


	@Test
	public void createAndRetrieveData() throws Exception {
		JSONObject reqBody = new JSONObject();
		reqBody.put("tag", "MINE");
		reqBody.put("description", "The manager of my cases");
		reqBody.put("name", "MyCaseManager 3.0");

		MockHttpServletResponse response = doCreate(CaseManagementSystem.class, reqBody);
		String caseManagerUrl = response.getRedirectedUrl();
		assertEquals("http://localhost" + basePath + "/caseManagementSystems/MINE", caseManagerUrl);
		mvc.perform(get(caseManagerUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("name").value("MyCaseManager 3.0"))
			.andExpect(jsonPath("$._links.self.href").value(caseManagerUrl))
			;

		reqBody = new JSONObject();
		reqBody.put("tag", "W-2");
		reqBody.put("description", "Everybody's favorite form");
		reqBody.put("name", "Salary Income Report");
		response = doCreate(CaseType.class, reqBody);
		String caseTypeUrl = response.getRedirectedUrl();
		assertEquals("http://localhost" + basePath + "/caseTypes/W-2", caseTypeUrl);
		mvc.perform(get(caseTypeUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("tag").value("W-2"))
			.andExpect(jsonPath("_links.self.href").value(caseTypeUrl))
		;
		reqBody = new JSONObject();
		reqBody.put("caseManagementSystem", caseManagerUrl);
		reqBody.put("caseType", caseTypeUrl);
		reqBody.put("receiptNumber", "123456");
		reqBody.put("caseCreation", "2001-01-01T14:00:32-05:00");
		String caseUrl = doCreate(TroubleCase.class, reqBody).getRedirectedUrl();

		reqBody = new JSONObject();
		reqBody.put("issueCase", caseUrl);
		reqBody.put("issueCreated", "2019-07-05T15:23:00-04:00");
		reqBody.put("issueType", "Super old");
		response = doCreate(CaseIssue.class, reqBody);
		String issueUrl = response.getRedirectedUrl();
		mvc.perform(get(issueUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("issueClosed").isEmpty());
		reqBody = new JSONObject();
		reqBody.put("issueClosed", "2019-07-10T18:11:00-04:00");
		mvc.perform(patch(issueUrl).content(reqBody.toString()))
			.andExpect(status().is(HttpServletResponse.SC_NO_CONTENT))
		;
		mvc.perform(get(issueUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("issueClosed").value("2019-07-10T18:11:00-04:00"))
		;
		reqBody = new JSONObject();
		reqBody.put("snoozeCase", caseUrl);
		reqBody.put("snoozeReason", "In the Mail");
		reqBody.put("snoozeDetails", "USPS123452345"); // fake tracking number, don't think too hard about it
		reqBody.put("snoozeStart", "2019-07-11T00:00:00-00:00");
		reqBody.put("snoozeEnd", "2019-07-18T00:00:00-00:00");

		String snoozeUrl = doCreate(CaseSnooze.class, reqBody).getRedirectedUrl();
		mvc.perform(get(snoozeUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("snoozeReason").value("In the Mail"))
		;
	}

	private MockHttpServletResponse doCreate(Class<?> entityType, JSONObject body) throws Exception {
		MockHttpServletRequestBuilder postRequest = post(links.linkFor(entityType).toUri())
			.content(body.toString());
		return mvc.perform(postRequest)
			.andExpect(status().is(HttpServletResponse.SC_CREATED))
			.andReturn()
			.getResponse()
			;
	}

}
