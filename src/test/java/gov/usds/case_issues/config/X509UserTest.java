package gov.usds.case_issues.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.x509;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.authorization.CaseIssuePermission;
import gov.usds.case_issues.controllers.CaseDetailsApiController;
import gov.usds.case_issues.controllers.ControllerTestBase;
import gov.usds.case_issues.controllers.UserInformationApiController;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;

/**
 * Tests for the behavior of the application when presented with an x509 certificate as user authentication.
 *
 */
@ActiveProfiles("auth-testing")
public class X509UserTest extends ControllerTestBase {

	private static final String SNOOZE_TEMPLATE = CaseDetailsApiController.URL_TEMPLATE + "/activeSnooze";
	private static final String CASE_SYS = "X";
	private static final String CASE_TYPE = "PREAUTH";
	private static final String RCPT = "ABC123UNME";

	@Before
	public void reset() {
		truncateDb();
	}

	@Test
	public void getUserInformation_withNormalUser_userInfoFound() throws Exception {
		perform(getUserInfoWith("normal-user-name"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("normal user name"))
			.andExpect(jsonPath("$.id").value(Matchers.startsWith("UID=be3bde871002e9fc83b0a387f485f363, CN=normal user name")))
			.andExpect(jsonPath("$.id").value(Matchers.endsWith("C=US")))
			;
	}

	@Test
	public void getUserInformation_withLongUserDn_userInfoFound() throws Exception { // this user has a DN longer than 100 but under 255 characters
		perform(getUserInfoWith("medium-long-user-name"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("medium-long-username"))
			.andExpect(jsonPath("$.id").value(Matchers.startsWith("UID=0bc6c46ca38ef3e8af3e50bc7ac244f7, CN=medium-long-username")))
			.andExpect(jsonPath("$.id").value(Matchers.endsWith("C=US")))
			;
	}

	@Test(expected=IllegalArgumentException.class)
	public void getUserInformation_withExtremeUserDn_serverError() throws Exception { // this user has a DN longer than 256 characters
		perform(getUserInfoWith("extra-long-user-name"));
	}

	@Test
	public void getAuthDetails_noAuthoritiesUser_noAuthoritiesFound() throws Exception {
		perform(getAuthDebugWith("normal-user-name"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authorities").isArray())
			.andExpect(jsonPath("$.authorities").isEmpty())
			;
	}

	@Test
	public void getAuthDetails_readUpdateManageUser_authoritiesFound() throws Exception {
		Matcher<Iterable<? extends String>> expectedPermissions = permissionSet(
				CaseIssuePermission.READ_CASES, CaseIssuePermission.UPDATE_CASES, CaseIssuePermission.MANAGE_APPLICATION);
		perform(getAuthDebugWith("alice"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authorities").isArray())
			.andExpect(jsonPath("$.authorities").value(expectedPermissions))
			;
	}

	@Test
	public void getAuthDetails_uploadUser_authoritiesFound() throws Exception {
		Matcher<Iterable<? extends String>> expectedPermissions = permissionSet(
				CaseIssuePermission.UPDATE_ISSUES);
		perform(getAuthDebugWith("bob"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.authorities").isArray())
			.andExpect(jsonPath("$.authorities").value(expectedPermissions))
			;
	}

	@Test
	public void snoozeCase_updateUser_csrfRequired() throws Exception {
		initData();
		perform(getSnoozeAsUser("alice")).andExpect(status().isNoContent());
		MockHttpServletRequestBuilder snoozePut = updateSnoozeAsUser("alice");
		perform(snoozePut).andExpect(status().isForbidden());
		perform(snoozePut.with(csrf())).andExpect(status().isOk());
	}

	@Test
	public void snoozeCase_noUpdateUser_forbidden() throws Exception {
		initData();
		MockHttpServletRequestBuilder snoozePut = updateSnoozeAsUser("bob");
		perform(snoozePut).andExpect(status().isForbidden());
		perform(snoozePut.with(csrf())).andExpect(status().isForbidden());
	}

	private MockHttpServletRequestBuilder getSnoozeAsUser(String certName) throws IOException, CertificateException {
		return get(SNOOZE_TEMPLATE, CASE_SYS, RCPT).with(x509(certName + ".crt"));
	}

	private MockHttpServletRequestBuilder updateSnoozeAsUser(String certName) throws IOException, CertificateException {
		JSONObject snooze = new JSONObject()
			.put("reason", "NAPTIME")
			.put("duration", 2)
			;
		return put(SNOOZE_TEMPLATE, CASE_SYS, RCPT)
			.content(snooze.toString())
			.contentType("application/json")
			.with(x509(certName + ".crt"))
			;
	}

	private void initData() {
		CaseManagementSystem sys = _dataService.ensureCaseManagementSystemInitialized(CASE_SYS, "X marks the spot");
		CaseType typ = _dataService.ensureCaseTypeInitialized(CASE_TYPE, "Pre-authenticated test case type");
		_dataService.initCaseAndOpenIssue(sys, RCPT, typ, ZonedDateTime.now().minusDays(2), "ARBITRARY");
	}

	private Matcher<Iterable<? extends String>> permissionSet(CaseIssuePermission... expected) {
		Collection<Matcher<? super String>> matchers = Arrays.stream(expected)
				.map(p -> Matchers.equalTo(p.name()))
				.collect(Collectors.toList());
		return Matchers.containsInAnyOrder(matchers);
	}

	private MockHttpServletRequestBuilder getUserInfoWith(String certName) throws IOException, CertificateException {
		return get(UserInformationApiController.USER_INFO_ENDPOINT).with(x509(certName + ".crt"));
	}

	private MockHttpServletRequestBuilder getAuthDebugWith(String certName) throws IOException, CertificateException {
		return get("/auth-info").with(x509(certName + ".crt"));
	}
}
