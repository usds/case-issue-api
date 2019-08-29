package gov.usds.case_issues.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import gov.usds.case_issues.authorization.CaseIssuePermission;
import gov.usds.case_issues.authorization.NamedOAuth2User;

@Configuration
public class OAuthMappingConfig {

	private static final Logger LOG = LoggerFactory.getLogger(OAuthMappingConfig.class);

	@Autowired
	private OAuth2CustomizationProperties mappedConfig;
	@Autowired(required=false) //  this is a sneaky way of making the bean conditional: being less sneaky would be better
	private OAuth2ClientProperties clientConfig;

	@Bean
	public WebSecurityPlugin oauthConfigurer() {
		LOG.info("Preparing custom OAuth2 user service configuration");
		if (clientConfig == null || clientConfig.getRegistration().isEmpty()) {
			return null; // see above "sneaky" remark
		}
		final GrantedAuthoritiesMapper mapper = oauthAuthorityMapper();
		final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		final List<String> namePath = Collections.unmodifiableList(
				new ArrayList<>(mappedConfig.getNamePath()));

		final OAuth2UserService<OAuth2UserRequest, OAuth2User> userService = namePath.isEmpty()
				? null
				: r -> {
					OAuth2User wrapped = delegate.loadUser(r);
					Optional<String> nameAttr = descend(wrapped.getAttributes(), namePath)
							.filter(v -> v instanceof String)
							.map(String.class::cast)
							;
					if (nameAttr.isPresent()) {
						return new NamedOAuth2User(nameAttr.get(), wrapped);
					} else {
						return null;
					}
				}
		;
		return http -> {
			LOG.info("Configuring OAuth user info service");
			try {
				OAuth2LoginConfigurer<HttpSecurity> oauth2Login = http.oauth2Login();
				if (userService != null) {
					oauth2Login.userInfoEndpoint().userService(userService);
				} else {
					LOG.info("No custom username mapping provided: using fallback service");
				}
				if (mapper != null) {
					oauth2Login.userInfoEndpoint().userAuthoritiesMapper(mapper);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	private GrantedAuthoritiesMapper oauthAuthorityMapper() {
		LOG.info("Building authority mapper from {}", mappedConfig.authorityPaths);
		// this is a shallow copy, so it's not as isolated as it should be
		final List<AuthorityPath> authorityPaths = new ArrayList<>(mappedConfig.authorityPaths);
		if (authorityPaths.isEmpty()) {
			LOG.error("No authority mapping configuration found");
			return null;
		}
		return authorities -> {
			List<GrantedAuthority> translated = new ArrayList<>();
			LOG.debug("Mapping authorities from {}", authorities);
			for (GrantedAuthority g : authorities) {
				LOG.debug("Input authority is {} (type {})", g, g.getClass());
				if (g instanceof OAuth2UserAuthority) {
					Map<String, Object> attributes = ((OAuth2UserAuthority) g).getAttributes();
					LOG.debug("Attributes to check: {}", attributes);
					for (AuthorityPath p : authorityPaths) {
						LOG.debug("Examining path {}", p.path);
						Optional<?> pathResult = descend(attributes, p.path);
						if (pathResult.isPresent()) {
							translated.add(p.getAuthority());
						}
					}
				}
			}
			return translated;
		};
	}

	@Component
	@ConfigurationProperties(prefix="oauth-user-config", ignoreUnknownFields=false)
	public static class OAuth2CustomizationProperties {
		public List<String> namePath = new ArrayList<>();
		public List<AuthorityPath> authorityPaths = new ArrayList<>();

		public List<String> getNamePath() {
			return namePath;
		}

		public void setNamePath(List<String> path) {
			namePath = path;
		}

		public List<AuthorityPath> getAuthorityPaths() {
			return authorityPaths;
		}

		public void setAuthorityPaths(List<AuthorityPath> authorityPaths) {
			this.authorityPaths = authorityPaths;
		}

	}

	public static class AuthorityPath {
		private CaseIssuePermission authority;
		private List<String> path;

		public CaseIssuePermission getAuthority() {
			return authority;
		}
		public void setAuthority(CaseIssuePermission name) {
			this.authority = name;
		}
		public List<String> getPath() {
			return path;
		}
		public void setPath(List<String> path) {
			this.path = path;
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Optional<?> descend(Object o, List<String> path) {
		Object curr = o;
		for (String pathElement : path) {
			LOG.debug("Looking for {} in {}", pathElement, curr);
			if (curr instanceof Map) {
				Map<String, Object> cast = Map.class.cast(curr);
				curr = cast.get(pathElement);
			}
			else if (curr instanceof Collection) {
				return ((Collection<String>) curr).stream().filter(pathElement::equals).findFirst();
			}
			else if (curr instanceof String) {
				return pathElement.equals(curr) ? Optional.of((String) curr) : Optional.empty();
			}
		}
		LOG.debug("Finished path traversal with {}", curr);
		if (curr instanceof Collection && ((Collection) curr).size() == 1) {
			Iterator<String> it = ((Iterable) curr).iterator();
			return it.hasNext() ? Optional.of(it.next()) : Optional.empty();
		} else {
			return Optional.ofNullable(curr);
		}
	}

}
