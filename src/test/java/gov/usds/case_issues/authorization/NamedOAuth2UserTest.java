package gov.usds.case_issues.authorization;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class NamedOAuth2UserTest {

	@Test
	public void user_toSrting_containsNameAndId() throws IOException, ServletException {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("name", "Emma Lazarus");
		attributes.put("user_id", "fe1ec665-82de-48e7-9ef3-fc28bdcc9d20");

		NamedOAuth2User user = new NamedOAuth2User(
			"not the name",
			Collections.singleton(new SimpleGrantedAuthority("respect")),
			attributes
		);
		assertEquals(user.toString(), "fe1ec665-82de-48e7-9ef3-fc28bdcc9d20 Emma Lazarus");
	}

}

