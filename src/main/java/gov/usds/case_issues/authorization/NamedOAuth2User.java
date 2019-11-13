package gov.usds.case_issues.authorization;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import gov.usds.case_issues.db.model.User;
import gov.usds.case_issues.db.repositories.UserRepository;

/**
 * A simple {@link OAuth2User} implementation that receives its name from outside,
 * rather than looking it up in the attributes with a simple key.
 * It should probably extend DefaultOAuth2User or borrow more of the internal structure
 * from that class, to make equality/hashing work reliably.
 */
public class NamedOAuth2User implements OAuth2User {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private String name;
	private Collection<? extends GrantedAuthority> authorities;
	private Map<String, Object> attributes;

	public NamedOAuth2User(String name, OAuth2User wrapped, UserRepository userRepo) {
		this.name = name;
		this.authorities =  wrapped.getAuthorities();
		this.attributes = wrapped.getAttributes();
		updateUsers(userRepo);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public String toString() {
		return name;
	}

	private void updateUsers(UserRepository userRepo) {
		User existingUser = userRepo.findByUserId(name);
		if (existingUser != null) {
			existingUser.updateLastSeen();
			userRepo.save(existingUser);
			return;
		}
		User newUser = new User(name, getAttributeName());
		userRepo.save(newUser);
	}

	private String getAttributeName() {
		Object attributeName = attributes.get("name");
		return attributeName.toString();
	}
}
