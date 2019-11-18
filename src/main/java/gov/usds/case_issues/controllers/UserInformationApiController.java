package gov.usds.case_issues.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.services.UserService;

/**
 * Controller to see information about the current user.
 */
@RestController
@Profile("auth-testing")
public class UserInformationApiController {

	@Autowired
	private UserService _userService;

	public static final String USER_INFO_ENDPOINT = "/api/users";

	@GetMapping(UserInformationApiController.USER_INFO_ENDPOINT)
	public HashMap<String, String> getCurrentUser(Authentication auth) {
		return _userService.getCurrentUser(auth);
	}

	@GetMapping(UserInformationApiController.USER_INFO_ENDPOINT + "/loggedin")
	public ResponseEntity<?> getUserLoggedin() {
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
