package gov.usds.case_issues.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import gov.usds.case_issues.authorization.CaseIssuePermission;

@Component
@ConfigurationProperties(prefix="web-customization", ignoreUnknownFields=false)
public class WebConfigurationProperties {

	private String[] _corsOrigins;
	private List<UserDefinition> _users;

	public void setCorsOrigins(String[] origins) {
		_corsOrigins = origins;
	}

	public String[] getCorsOrigins() {
		return _corsOrigins;
	}

	public List<UserDefinition> getUsers() {
		return _users == null ? Collections.emptyList() : _users;
	}

	public void setUsers(List<UserDefinition> users) {
		this._users = users;
	}

	public static class UserDefinition {
		private String _name;
		private List<CaseIssuePermission> _grants = new ArrayList<>();

		public String getName() {
			return _name;
		}
		public void setName(String _name) {
			this._name = _name;
		}

		public List<CaseIssuePermission> getGrants() {
			return _grants;
		}
		public void setGrants(List<CaseIssuePermission> _grants) {
			this._grants = _grants;
		}
	}
}
