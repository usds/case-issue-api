package gov.usds.case_issues.controllers;

import java.util.HashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to see information about the current user.
 */
@RestController
@RequestMapping("/api/users")
@Profile("auth-testing")
public class UserDetailsApiController {

	@GetMapping
	public Object getUser(Authentication u) {
		HashMap<String, String> user = new HashMap<String, String>();
		user.put("Name", u.getName());
		return user;
	}
}
