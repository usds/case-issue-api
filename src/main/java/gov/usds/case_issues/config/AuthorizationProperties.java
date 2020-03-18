package gov.usds.case_issues.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import gov.usds.case_issues.config.model.AuthorityMapping;

/**
 * Generalized container the properties needed for mapping externally-defined users to internal user
 * data structures and permission grants.
 */
@Component
@ConfigurationProperties(prefix="authorization", ignoreUnknownFields=false)
public class AuthorizationProperties {

	private List<String> oauthIdPath = new ArrayList<>();

	private List<AuthorityMapping> authorities = new ArrayList<>();

	public List<String> getOauthIdPath() {
		return oauthIdPath;
	}

	public void setOauthIdPath(List<String> oauthIdPath) {
		this.oauthIdPath = oauthIdPath;
	}

	public List<AuthorityMapping> getGrants() {
		return authorities;
	}

	public void setGrants(List<AuthorityMapping> authorities) {
		this.authorities = authorities;
	}
}
