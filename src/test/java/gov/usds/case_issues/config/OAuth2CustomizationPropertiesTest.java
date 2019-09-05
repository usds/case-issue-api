package gov.usds.case_issues.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import gov.usds.case_issues.authorization.CaseIssuePermission;
import gov.usds.case_issues.config.OAuthMappingConfig.AuthorityPath;
import gov.usds.case_issues.config.OAuthMappingConfig.OAuth2CustomizationProperties;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class OAuth2CustomizationPropertiesTest extends CaseIssueApiTestBase {

	@Autowired
	private Environment env;

	@Test
	public void bindProperties_validData_translationCorrect() {
		BindResult<OAuth2CustomizationProperties> bound = bindProperties("bind-testing.oauth-conf-a");
		assertTrue(bound.isBound());
		OAuth2CustomizationProperties props = bound.get();
		assertEquals(Arrays.asList("yabba","dabba", "doo"), props.getNamePath());
		List<AuthorityPath> authorityPaths = props.getAuthorityPaths();
		assertEquals(1, authorityPaths.size());
		assertEquals(CaseIssuePermission.UPDATE_ISSUES, authorityPaths.get(0).getAuthority());
		assertEquals(Arrays.asList("custom","attribute","path"), authorityPaths.get(0).getPath());
	}

	private BindResult<OAuthMappingConfig.OAuth2CustomizationProperties> bindProperties(String baseProperty) {
		return Binder.get(env).bind(baseProperty, OAuthMappingConfig.OAuth2CustomizationProperties.class);
	}
}
