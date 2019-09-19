package gov.usds.case_issues.controllers;

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

	public static final String USER_INFO_ENDPOINT = "/api/users";

	@GetMapping
	public Object getUser(Authentication u) {
		return u;
	}
}
