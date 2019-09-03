package gov.usds.case_issues.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import gov.usds.case_issues.authorization.CaseIssuePermission;

public class OAuthMappingConfigTest {

	private static final List<String> NEVER_FOUND = Arrays.asList("no_such_attribute","nopenopenope");

	@Test
	public void oauthAuthorityMapper_emptyInput_nullMapper() {
		assertNull(OAuthMappingConfig.oauthAuthorityMapper(null));
		assertNull(OAuthMappingConfig.oauthAuthorityMapper(Collections.emptyList()));
	}

	@Test
	public void oauthAuthorityMapper_noAttributes_noAuthorities() {
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(
			Collections.singletonList(new OAuthMappingConfig.AuthorityPath(CaseIssuePermission.READ_CASES, NEVER_FOUND)));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> inputAuthorities= Collections.singleton(
				new OAuth2UserAuthority(Collections.singletonMap("my_user_key", "my_user_value"))
		);
		assertEquals(0, mapper.mapAuthorities(inputAuthorities).size());
	}

	@Test
	public void oauthAuthorityMapper_emptyPath_authorityFound() {
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(
			Collections.singletonList(new OAuthMappingConfig.AuthorityPath(CaseIssuePermission.READ_CASES, Collections.emptyList())));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> inputAuthorities= Collections.singleton(
				new OAuth2UserAuthority(Collections.singletonMap("my_user_key", "my_user_value"))
		);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(inputAuthorities);
		assertEquals(1, mapped.size());
		assertTrue(mapped.contains(CaseIssuePermission.READ_CASES));
	}

	@Test
	public void oauthAuthorityMapper_pathToNull_authorityNotFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("no_such_key"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(0, mapped.size());
	}

	@Test
	public void oauthAuthorityMapper_pathToMap_authorityFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("my_attr"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(1, mapped.size());
		assertTrue(mapped.contains(CaseIssuePermission.READ_CASES));
	}

	@Test
	public void oauthAuthorityMapper_pathToValidScalar_authorityFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("my_attr", "scalar_name", "name_in_scalar"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(1, mapped.size());
		assertTrue(mapped.contains(CaseIssuePermission.READ_CASES));
	}

	@Test
	public void oauthAuthorityMapper_pathToInvalidScalar_authorityNotFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("my_attr", "scalar_name", "NOT CORRECT"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(0, mapped.size());
	}

	@Test
	public void oauthAuthorityMapper_pathToValidListElement_authorityFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("my_attr", "name_list", "actual_name"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(1, mapped.size());
		assertTrue(mapped.contains(CaseIssuePermission.READ_CASES));
	}
	@Test
	public void oauthAuthorityMapper_pathToInvalidListElement_authorityNotFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("my_attr", "name_list", "NOT ACTUAL NAME"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(0, mapped.size());
	}

	@org.junit.Ignore // I am not sure what the correct behavior should be here
	@Test
	public void oauthAuthorityMapper_pathToEmptyList_authorityNotFound() {
		OAuthMappingConfig.AuthorityPath path = new OAuthMappingConfig.AuthorityPath(
				CaseIssuePermission.READ_CASES, Arrays.asList("my_attr", "empty_list"));
		GrantedAuthoritiesMapper mapper = OAuthMappingConfig.oauthAuthorityMapper(Collections.singletonList(path));
		assertNotNull(mapper);
		Collection<? extends GrantedAuthority> mapped = mapper.mapAuthorities(simpleAuthority());
		assertEquals(0, mapped.size());
	}

	@Test
	public void createDelegatingUserService_emptyInput_nullService() {
		assertNull(OAuthMappingConfig.createDelegatingUserService(null, null));
		assertNull(OAuthMappingConfig.createDelegatingUserService(null, Collections.emptyList()));
	}

	@Test
	public void createDelegatingUserService_nameInList_correctUser() {
		Map<String, Object> attr = simpleAttributes();
		OAuth2UserService<OAuth2UserRequest, OAuth2User> service = setupService("my_attr", "name_list");
		OAuth2User user = service.loadUser(null); // we ignore the input anyway
		assertEquals("actual_name", user.getName());
		assertEquals(attr, user.getAttributes());
		assertEquals(Collections.singleton(new SimpleGrantedAuthority("respect")), user.getAuthorities());
	}

	@Test
	public void createDelegatingUserService_nameInScalar_correctUser() {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> service = setupService("my_attr", "scalar_name");
		OAuth2User user = service.loadUser(null);
		assertEquals("name_in_scalar", user.getName());
		assertEquals(simpleAttributes(), user.getAttributes());
		assertEquals(Collections.singleton(new SimpleGrantedAuthority("respect")), user.getAuthorities());
	}

	@Test(expected=IllegalArgumentException.class)
	public void createDelegatingUserService_pathToNull_error() {
		setupService("my_attr", "nopenopenope").loadUser(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void createDelegatingUserService_pathToMap_error() {
		setupService("my_attr").loadUser(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void createDelegatingUserService_pathToEmptyList_error() {
		setupService("my_attr", "empty_list").loadUser(null);
	}

	private OAuth2UserService<OAuth2UserRequest, OAuth2User> setupService(String... namePath) {
		return OAuthMappingConfig.createDelegatingUserService(
			r->new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("respect")), simpleAttributes(), "name"),
			Arrays.asList(namePath)
		);
	}
	private Set<OAuth2UserAuthority> simpleAuthority() {
		return Collections.singleton(new OAuth2UserAuthority(simpleAttributes()));
	}

	private Map<String, Object> simpleAttributes() {
		Map<String, Object> attr = new HashMap<>();
		attr.put("name", "not actually the name");
		Map<String, Object> myAttr = new HashMap<>();
		myAttr.put("name_list", Arrays.asList("actual_name"));
		myAttr.put("empty_list", Collections.emptyList());
		myAttr.put("scalar_name", "name_in_scalar");
		attr.put("my_attr", myAttr);
		return attr;
	}
}
