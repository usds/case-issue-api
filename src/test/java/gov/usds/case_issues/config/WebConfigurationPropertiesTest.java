package gov.usds.case_issues.config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import gov.usds.case_issues.authorization.CaseIssuePermission;
import gov.usds.case_issues.config.WebConfigurationProperties;
import gov.usds.case_issues.config.WebConfigurationProperties.UserDefinition;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class WebConfigurationPropertiesTest extends CaseIssueApiTestBase {

	@Autowired
	private Environment env;

	@Test
	public void bindProperties_validCorsNoUsers_dataFound() {
		BindResult<WebConfigurationProperties> configBind = bindProperties("bind-testing.web-conf-a");
		assertTrue(configBind.isBound());
		WebConfigurationProperties props = configBind.get();
		assertArrayEquals(new String[] {"http://origin", "https://other-origin"},
			props.getCorsOrigins());
		assertNotNull(props.getUsers());
		assertTrue(props.getUsers().isEmpty());
	}

	@Test
	public void bindProperties_noProps_notBound() {
		BindResult<WebConfigurationProperties> configBind = bindProperties("nope-nope-npe");
		assertFalse(configBind.isBound());
	}

	@Test
	public void bindProperties_validUsers_correctUsersFound() {
		BindResult<WebConfigurationProperties> bound = bindProperties("bind-testing.web-conf-b");
		assertTrue(bound.isBound());
		List<UserDefinition> users = bound.get().getUsers();
		assertNotNull(users);
		assertEquals(2, users.size());
		assertEquals("user1", users.get(0).getName());
		HashSet<CaseIssuePermission> foundGrants = new HashSet<>(users.get(0).getGrants());
		HashSet<CaseIssuePermission> allGrants = new HashSet<>(Arrays.asList(
				CaseIssuePermission.READ_CASES, CaseIssuePermission.UPDATE_CASES,
				CaseIssuePermission.UPDATE_ISSUES, CaseIssuePermission.UPDATE_STRUCTURE)
		);
		assertEquals(allGrants, foundGrants);
		assertEquals("user2", users.get(1).getName());
		assertTrue(users.get(1).getGrants().isEmpty());
	}

	@Test(expected=BindException.class)
	public void bindProperties_invalidGrant_error() {
		bindProperties("bind-testing.web-conf-c");
	}

	private BindResult<WebConfigurationProperties> bindProperties(String baseProperty) {
		return Binder.get(env).bind(baseProperty, WebConfigurationProperties.class);
	}
}
