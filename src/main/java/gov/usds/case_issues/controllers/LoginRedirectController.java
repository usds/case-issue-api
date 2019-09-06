package gov.usds.case_issues.controllers;

import java.net.URI;

import org.hibernate.validator.constraints.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import gov.usds.case_issues.config.WebConfigurationProperties;

@Controller
@Validated
public class LoginRedirectController {

	private static final Logger LOG = LoggerFactory.getLogger(LoginRedirectController.class);

	@Autowired
	private WebConfigurationProperties config;

	@GetMapping("/clientLogin")
	public ResponseEntity<String> redirectToClient(@RequestParam(required=false) @URL String redirect) {
		String[] corsOrigins = config.getCorsOrigins();
		if (corsOrigins == null || corsOrigins.length == 0) {
			return ResponseEntity.ok("Login successful, but client redirection is not currently configured.");
		}
		String validatedRedirect = null;
		if (redirect == null) {
			LOG.debug("No redirect provided: defaulting to {}", corsOrigins[0]);
			validatedRedirect = corsOrigins[0];
		} else {
			for (String allowedOrigin : corsOrigins) {
				LOG.debug("Checking {} against {}", redirect, allowedOrigin);
				// prevent "http://good.origin:shenanigans/
				String allowedPrefix = allowedOrigin.endsWith("/") ? allowedOrigin : allowedOrigin + "/";
				if (redirect.equals(allowedOrigin) || redirect.startsWith(allowedPrefix)) {
					validatedRedirect = redirect;
					LOG.debug("Match found!");
					break;
				}
			}
		}
		if (validatedRedirect == null) {
			throw new IllegalArgumentException("Illegal  redirect parameter.");
		}
		return ResponseEntity
			.status(HttpStatus.FOUND)
			.location(URI.create(validatedRedirect))
			.build();
	}
}
