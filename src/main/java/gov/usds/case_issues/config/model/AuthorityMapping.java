package gov.usds.case_issues.config.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usds.case_issues.authorization.CaseIssuePermission;

/**
 * A property container for all the different ways we can issue authorities to a user.
 */
public class AuthorityMapping {

	private static final Logger LOG = LoggerFactory.getLogger(AuthorityMapping.class);

	private AuthenticationType _authenticationType;
	// these are mostly for demo users but are nice placeholders for debugging
	private String _name;
	private String _description;
	private Set<CaseIssuePermission> _authorities;
	private List<String> _matchCondition;
	private String _joinedCondition = null;
	private boolean _bypassCsrf;
	private boolean _terminal;

	public AuthenticationType getAuthenticationType() {
		return _authenticationType;
	}

	public Set<CaseIssuePermission> getAuthorities() {
		return _authorities;
	}

	public List<String> getMatchCondition() {
		return _matchCondition;
	}

	public String getMatchString() {
		if (null == _joinedCondition) {
			_joinedCondition = String.join(", ", _matchCondition);
			LOG.debug("Joined back to [{}]", _joinedCondition);
		}
		return _joinedCondition;
	}

	public void setAuthenticationType(AuthenticationType authenticationType) {
		this._authenticationType = authenticationType;
	}

	public void setAuthorities(Set<CaseIssuePermission> authorities) {
		this._authorities = Collections.unmodifiableSet(EnumSet.copyOf(authorities));
	}

	public void setMatchCondition(List<String> matchCondition) {
		this._matchCondition = Collections.unmodifiableList(new ArrayList<>(matchCondition));
	}

	public void setTerminal(boolean terminal) {
		_terminal = terminal;
	}

	/** If a match is found, does that mean no further matches need to be looked for? */
	public boolean isTerminal() {
		return _terminal;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	/** Do users set up under this authority get to bypass CSRF protection? */
	public boolean isBypassCsrf() {
		return _bypassCsrf;
	}

	public void setBypassCsrf(boolean bypassCsrf) {
		this._bypassCsrf = bypassCsrf;
	}
}
