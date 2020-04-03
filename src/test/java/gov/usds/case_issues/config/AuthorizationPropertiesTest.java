package gov.usds.case_issues.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import gov.usds.case_issues.authorization.CaseIssuePermission;
import gov.usds.case_issues.config.model.AuthenticationType;
import gov.usds.case_issues.config.model.AuthorityMapping;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class AuthorizationPropertiesTest extends CaseIssueApiTestBase {

	@Autowired
	private Environment env;

	@Test
	public void bindProperties_validTestMappings_correctMappingsFound() {
		BindResult<AuthorizationProperties> bound = bindProperties("bind-testing.auth-conf-a");
		assertTrue(bound.isBound());
		List<AuthorityMapping> mappings = bound.get().getGrants().get(AuthenticationType.TEST);
		assertNotNull(mappings);
		assertEquals(2, mappings.size());
		assertEquals("user1", mappings.get(0).getName());
		Set<CaseIssuePermission> foundGrants = mappings.get(0).getAuthorities();
		Set<CaseIssuePermission> allGrants = new HashSet<>(Arrays.asList(
				CaseIssuePermission.READ_CASES, CaseIssuePermission.UPDATE_CASES,
				CaseIssuePermission.UPDATE_ISSUES, CaseIssuePermission.UPDATE_STRUCTURE)
		);
		assertEquals(allGrants, foundGrants);
		assertEquals("user2", mappings.get(1).getName());
		assertTrue(mappings.get(1).getAuthorities().isEmpty());
	}

	@Test(expected=BindException.class)
	public void bindProperties_invalidAuthority_error() {
		bindProperties("bind-testing.auth-conf-b");
	}

	@Test(expected=BindException.class)
	public void bindProperties_invalidAuthenticationType_error() {
		bindProperties("bind-testing.auth-conf-c");
	}

	@Test
	public void bindProperties_validX509Mappings_correctMappingsFound() {
		BindResult<AuthorizationProperties> bound = bindProperties("bind-testing.auth-conf-d");
		assertTrue(bound.isBound());
		List<AuthorityMapping> mappings = bound.get().getGrants().get(AuthenticationType.X509);
		assertEquals("a, b, c, d", mappings.get(0).getMatchString());
		assertEquals(Collections.singleton(CaseIssuePermission.MANAGE_APPLICATION), mappings.get(0).getAuthorities());
		assertEquals("q, w, e, rty", mappings.get(1).getMatchString());
		assertEquals(Collections.singleton(CaseIssuePermission.READ_CASES), mappings.get(1).getAuthorities());
	}

	@Test
	public void bindProperties_validOauthMappings_correctMappingsFound() {
		BindResult<AuthorizationProperties> bound = bindProperties("bind-testing.auth-conf-e");
		assertTrue(bound.isBound());
		assertEquals(Arrays.asList("yabba","dabba", "doo"), bound.get().getOauthIdPath());
		List<AuthorityMapping> mappings = bound.get().getGrants().get(AuthenticationType.OAUTH);
		assertEquals(Collections.singleton(CaseIssuePermission.UPDATE_ISSUES), mappings.get(0).getAuthorities());
		assertEquals(Arrays.asList("custom","attribute","path"), mappings.get(0).getMatchCondition());
	}

	private BindResult<AuthorizationProperties> bindProperties(String baseProperty) {
		return Binder.get(env).bind(baseProperty, AuthorizationProperties.class);
	}
}
