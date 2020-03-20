package gov.usds.case_issues.authorization;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * A special {@link User} subclass for pre-authenticated (usually service-level) users.
 * Identical to the base class except in that it permits users to be declared as exempt
 * from CSRF protection (to simplify non-browser clients).
 */
public class PreauthUser extends User {

	private static final long serialVersionUID = 1L;

	private boolean _bypassCsrf;

	public PreauthUser(String username, Collection<? extends GrantedAuthority> authorities, boolean bypassCsrf) {
		super(username, "", authorities);
	}

	/**
	 * Returns true if this user is allowed to ignore CSRF protection (this should only
	 * be true if the user is a non-browser client).
	 */
	public boolean canBypassCsrf() {
		return _bypassCsrf;
	}
	
}
