package gov.usds.case_issues.controllers;

import java.util.HashMap;

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
	public Object getUser(Authentication u) {
		HashMap<String, String> user = new HashMap<String, String>();
		String name = u.getName();
		if (name.contains(";")) {
			String[] userInfo = name.split(";");
			user.put("ID", userInfo[0]);
			user.put("name", userInfo[1]);
		} else {
			user.put("name", u.getName());
		}
		return user;
	}

	@GetMapping(UserDetailsApiController.USER_INFO_ENDPOINT + "/loggedin")
	public ResponseEntity<?> getUserLoggedin() {
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
