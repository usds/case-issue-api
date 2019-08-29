package gov.usds.case_issues.authorization;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

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

	public NamedOAuth2User(String name, OAuth2User wrapped) {
		this(name, wrapped.getAuthorities(), wrapped.getAttributes());
	}

	public NamedOAuth2User(String name,
			Collection<? extends GrantedAuthority> authorities,
			Map<String, Object> attributes) {
		this.name = name;
		this.authorities = authorities;
		this.attributes = attributes;
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
}
