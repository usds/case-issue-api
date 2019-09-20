package gov.usds.case_issues.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to see information about the current user.
 */
@RestController
@RequestMapping(UserDetailsApiController.USER_INFO_ENDPOINT)
@Profile("auth-testing")
public class UserDetailsApiController {
	private static final Logger LOG = LoggerFactory.getLogger(UserDetailsApiController.class);

	public static final String USER_INFO_ENDPOINT = "/api/users";

	@GetMapping
	@SuppressWarnings({"unchecked"})
	public Object getUser(Authentication u) {
		HashMap<String, String> user = new HashMap<String, String>();
		user.put("ID", u.getName());
		try {
			Map<String, Object> principal = (Map<String, Object>)u.getPrincipal();
			Collection<Object> authorities = (Collection<Object>)principal.get("authorities");
			Map<String, Object> authority = (Map<String, Object>)authorities.iterator().next();
			Map<String, String> attributes = (Map<String, String>)authority.get("attributes");
			user.put("name", attributes.get("name"));
		} catch(Exception e) {
			LOG.error("Unable to get user name", e);
		}
		return user;
	}
}
