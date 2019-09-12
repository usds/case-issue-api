package gov.usds.case_issues.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

@WithMockUser(authorities = "UPDATE_STRUCTURE")
public class ResourceControllerTest extends ControllerTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceControllerTest.class);

	private static final String FIXTURE_FORM_TAG = "FORM_1";
	private static final String FIXTURE_CASE_MANAGER_TAG = "ME2";

	@Autowired
	private RepositoryEntityLinks links;
	@Value("${spring.data.rest.basePath}")
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
		_mvc.perform(get(caseManagerUrl))
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
		_mvc.perform(get(caseTypeUrl))
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
		_mvc.perform(get(issueUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("issueClosed").isEmpty());
		reqBody = new JSONObject();
		reqBody.put("issueClosed", "2019-07-10T18:11:00-04:00");
		_mvc.perform(patch(issueUrl).content(reqBody.toString()))
			.andExpect(status().is(HttpStatus.NO_CONTENT.value()))
		;

		@SuppressWarnings("checkstyle:MagicNumber")
		ZonedDateTime closedTime = ZonedDateTime.of(2019, 7, 10, 18, 11, 0, 0, ZoneId.of("-04:00"));
		String responseBody = _mvc.perform(get(issueUrl))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString()
		;
		ZonedDateTime issueClosedTime = ZonedDateTime.parse(
				new JSONObject(responseBody).getString("issueClosed"),
				DateTimeFormatter.ISO_OFFSET_DATE_TIME
		);
		assertTrue("Closed time as expected", issueClosedTime.isEqual(closedTime));

		reqBody = new JSONObject();
		reqBody.put("snoozeCase", caseUrl);
		reqBody.put("snoozeReason", "In the Mail");
		reqBody.put("snoozeDetails", "USPS123452345"); // fake tracking number, don't think too hard about it
		reqBody.put("snoozeStart", "2019-07-11T00:00:00-00:00");
		reqBody.put("snoozeEnd", "2019-07-18T00:00:00-00:00");

		String snoozeUrl = doCreate(CaseSnooze.class, reqBody).getRedirectedUrl();
		_mvc.perform(get(snoozeUrl))
			.andExpect(status().isOk())
			.andExpect(jsonPath("snoozeReason").value("In the Mail"))
		;
	}

	@Test
	public void invalidTagErrors() throws Exception {
		JSONObject reqBody = new JSONObject();
		reqBody.put("tag", "MINE/YOURS");
		reqBody.put("description", "This should never get inserted");
		reqBody.put("name", "Tagged Entity With Invalid Tag");
		doCreate(CaseManagementSystem.class, reqBody, HttpStatus.BAD_REQUEST);
		doCreate(CaseType.class, reqBody, HttpStatus.BAD_REQUEST);

		reqBody.put("tag", "1.2");
		doCreate(CaseManagementSystem.class, reqBody, HttpStatus.BAD_REQUEST);
		doCreate(CaseType.class, reqBody, HttpStatus.BAD_REQUEST);

		reqBody.put("tag", "");
		doCreate(CaseManagementSystem.class, reqBody, HttpStatus.BAD_REQUEST);
		doCreate(CaseType.class, reqBody, HttpStatus.BAD_REQUEST);
	}

	@Test
	public void invalidReceiptNumber() throws Exception {
		createFixtureEntities();
		JSONObject reqBody = new JSONObject();
		reqBody.put("caseType", getFixtureCaseTypeUrl());
		reqBody.put("caseManagementSystem", getFixtureCaseManagerUrl());
		reqBody.put("caseCreation", "1978-08-05T01:00:00-07:00");

		// null receipt
		doCreate(TroubleCase.class, reqBody, HttpStatus.BAD_REQUEST);
		reqBody.put("receiptNumber", "");
		doCreate(TroubleCase.class, reqBody, HttpStatus.BAD_REQUEST);
		reqBody.put("receiptNumber", "/hacker");
		doCreate(TroubleCase.class, reqBody, HttpStatus.BAD_REQUEST);
		reqBody.put("receiptNumber", "spaces are not allowed");
		doCreate(TroubleCase.class, reqBody, HttpStatus.BAD_REQUEST);
	}


	@Test
	public void duplicateTagErrors() throws Exception {
		JSONObject reqBody = new JSONObject();
		reqBody.put("tag", "ME3");
		reqBody.put("description", "The new manager of my cases");
		reqBody.put("name", "MyCaseManager 3.3");

		doCreate(CaseManagementSystem.class, reqBody);
		doCreate(CaseManagementSystem.class, reqBody, HttpStatus.CONFLICT);

		reqBody = new JSONObject();
		reqBody.put("tag", "F123");
		reqBody.put("description", "The new hot form");
		reqBody.put("name", "Standard Form 321");
		doCreate(CaseType.class, reqBody);
		reqBody.put("description", "Newest and hottest!");
		doCreate(CaseType.class, reqBody, HttpStatus.CONFLICT);
	}

	@Test
	public void duplicateReceiptScenarios() throws Exception {
		createFixtureEntities();
		JSONObject reqBody = new JSONObject();
		reqBody.put("caseType", getFixtureCaseTypeUrl());
		reqBody.put("caseManagementSystem", getFixtureCaseManagerUrl());
		reqBody.put("caseCreation", "1978-08-05T01:00:00-07:00");

		reqBody.put("receiptNumber", "A123456789");
		doCreate(TroubleCase.class, reqBody);
		doCreate(TroubleCase.class, reqBody, HttpStatus.CONFLICT);

	}

	@Test
	@WithAnonymousUser
	public void fetchCases_anonymous_forbidden() throws Exception {
		perform(get(linkFor(TroubleCase.class))).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser
	public void fetchCases_noAuthorities_forbidden() throws Exception {
		perform(get(linkFor(TroubleCase.class))).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser
	public void createCase_noAuthorities_forbidden() throws Exception {
		doCreate(TroubleCase.class, new JSONObject(), HttpStatus.FORBIDDEN);
	}

	@Test
	@WithMockUser(authorities = "UPDATE_CASES")
	public void createCase_writeCaseAuthority_forbidden() throws Exception {
		doCreate(TroubleCase.class, new JSONObject(), HttpStatus.FORBIDDEN);
	}

	private MockHttpServletResponse doCreate(Class<?> entityType, JSONObject body) throws Exception {
		return doCreate(entityType, body, HttpStatus.CREATED);
	}

	private MockHttpServletResponse doCreate(Class<?> entityType, JSONObject body, HttpStatus expectedStatus) throws Exception {
		MockHttpServletRequestBuilder postRequest = post(linkFor(entityType))
			.content(body.toString());
		return _mvc.perform(postRequest)
			.andExpect(status().is(expectedStatus.value()))
			.andReturn()
			.getResponse()
			;
	}

	private URI linkFor(Class<?> entityType) {
		return links.linkFor(entityType).toUri();
	}

	private void createFixtureEntities() throws Exception {
		MockHttpServletResponse caseManagerCheck = _mvc.perform(get(getFixtureCaseManagerUrl()))
				.andReturn().getResponse();
		if (caseManagerCheck.getStatus() == HttpStatus.NOT_FOUND.value()) {
			LOG.debug("(Re)creating fixture case manager");
			JSONObject reqBody = new JSONObject();
			reqBody.put("tag", FIXTURE_CASE_MANAGER_TAG);
			reqBody.put("description", "The new manager of my cases");
			reqBody.put("name", "MyCaseManager 3.1");

			doCreate(CaseManagementSystem.class, reqBody);
		}

		MockHttpServletResponse caseTypeCheck = _mvc.perform(get(getFixtureCaseTypeUrl()))
				.andReturn().getResponse();
		if (caseTypeCheck.getStatus() == HttpStatus.NOT_FOUND.value()) {
			LOG.debug("(Re)creating fixture case type");
			JSONObject reqBody = new JSONObject();
			reqBody.put("tag", FIXTURE_FORM_TAG);
			reqBody.put("description", "The new form for every possible request");
			reqBody.put("name", "Request For Any and Every Type of Benefit");
			doCreate(CaseType.class, reqBody);
		}
	}

	private URI getFixtureCaseManagerUrl() {
		return links.linkForSingleResource(CaseManagementSystem.class, FIXTURE_CASE_MANAGER_TAG).toUri();
	}

	private URI getFixtureCaseTypeUrl() {
		return links.linkForSingleResource(CaseType.class, FIXTURE_FORM_TAG).toUri();
	}
}
