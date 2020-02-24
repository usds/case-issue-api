package gov.usds.case_issues.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.model.SerializedUserInformation;
import gov.usds.case_issues.services.UserService;

/**
 * Controller to see information about the current user.
 */
@RestController
public class UserInformationApiController {

	@Autowired
	private UserService _userService;

	public static final String USER_INFO_ENDPOINT = "/api/users";

	@GetMapping(UserInformationApiController.USER_INFO_ENDPOINT)
	public SerializedUserInformation getCurrentUser(Authentication auth) {
		return _userService.getCurrentUser(auth);
	}
}
