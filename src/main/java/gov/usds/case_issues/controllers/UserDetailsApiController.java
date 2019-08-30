package gov.usds.case_issues.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

/**
 * Development-only controller to see information about the current user, to validate that
 * authentication and authorization are at least sort of working.
 */
@RestController
@RequestMapping("/user")
@Profile("auth-testing")
@ApiIgnore
public class UserDetailsApiController {

	@GetMapping
	public Object getUser(Authentication u) {
		return u;
	}
}
