package gov.usds.case_issues.controllers;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Painfully simple controller to allow clients to find out the CSRF token behaviors
 * that will be accepted by this service.
 */
@RestController
public class CsrfController {

	@GetMapping("/csrf")
	public CsrfToken getToken(CsrfToken t) {
		return t;
	}
}
