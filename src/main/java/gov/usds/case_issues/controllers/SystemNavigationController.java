package gov.usds.case_issues.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.model.NavigationEntry;
import gov.usds.case_issues.services.ApplicationMetadataService;

/**
 * Controller for API endpoints that the browser application will need in order to generate
 * navigation elements for the end user.
 */
@RestController
@RequestMapping("/api/navigation")
public class SystemNavigationController {

	@Autowired
	private ApplicationMetadataService _metadataService;

	@GetMapping
	public List<NavigationEntry> getNavigationInformation() {
		return _metadataService.getCaseNavigation();
	}
}
