package gov.usds.case_issues.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import gov.usds.case_issues.config.model.AuthenticationType;
import gov.usds.case_issues.config.model.AuthorityMapping;

/**
 * Generalized container the properties needed for mapping externally-defined users to internal user
 * data structures and permission grants.
 */
@Component
@ConfigurationProperties(prefix="authorization", ignoreUnknownFields=false)
public class AuthorizationProperties {

	private List<String> oauthIdPath = new ArrayList<>();

	private Map<AuthenticationType, List<AuthorityMapping>> authorities = new EnumMap<>(AuthenticationType.class);

	public List<String> getOauthIdPath() {
		return oauthIdPath;
	}

	public void setOauthIdPath(List<String> oauthIdPath) {
		this.oauthIdPath = oauthIdPath;
	}

	public Map<AuthenticationType, List<AuthorityMapping>> getGrants() {
		return authorities;
	}

	public void setGrants(Map<AuthenticationType, List<AuthorityMapping>> authorities) {
		this.authorities = authorities;
	}
}
