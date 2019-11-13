package gov.usds.case_issues.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.db.model.OAuthUser;
import gov.usds.case_issues.db.repositories.UserRepository;

/**
 * Controller to see information about the current user.
 */
@RestController
@Profile("auth-testing")
public class UserDetailsApiController {

	@Autowired
	private UserRepository _userRepo;

	public static final String USER_INFO_ENDPOINT = "/api/users";

	@GetMapping(UserDetailsApiController.USER_INFO_ENDPOINT)
	public Object getCurrentUser(Authentication auth) {
		String id = auth.getName();
		OAuthUser user = _userRepo.findByUserId(id);
		HashMap<String, Object> response = new HashMap<String, Object>();
		response.put("ID", user.getId());
		response.put("name", user.getPrintName());
		return response;
	}

	@GetMapping(UserDetailsApiController.USER_INFO_ENDPOINT + "/loggedin")
	public ResponseEntity<?> getUserLoggedin() {
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
