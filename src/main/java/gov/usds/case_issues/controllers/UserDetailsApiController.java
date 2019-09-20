package gov.usds.case_issues.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to see information about the current user.
 */
@RestController
@Profile("auth-testing")
public class UserDetailsApiController {

	public static final String USER_INFO_ENDPOINT = "/api/users";

	@GetMapping(UserDetailsApiController.USER_INFO_ENDPOINT)
	@SuppressWarnings({"unchecked"})
	public Object getUser(Authentication u) {
		HashMap<String, String> user = new HashMap<String, String>();
		user.put("ID", u.getName());
		Map<String, Object> principal = (Map<String, Object>)u.getPrincipal();
		Collection<Object> authorities = (Collection<Object>)principal.get("authorities");
		Map<String, Object> authority = (Map<String, Object>)authorities.iterator().next();
		Map<String, String> attributes = (Map<String, String>)authority.get("attributes");
		user.put("name", attributes.get("name"));
		return user;
	}

	@GetMapping(UserDetailsApiController.USER_INFO_ENDPOINT + "/loggedin")
	public ResponseEntity<?> getUserLoggedin() {
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
