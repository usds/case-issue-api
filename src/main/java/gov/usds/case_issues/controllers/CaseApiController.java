package gov.usds.case_issues.controllers;

import java.time.ZonedDateTime;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.db.model.CaseMetadata;
import gov.usds.case_issues.services.ApplicationMetadataService;


@RestController
@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).READ_CASES.name())")
@RequestMapping("/api/cases/")
@Validated
public class CaseApiController {

	@Autowired
	private ApplicationMetadataService _metadataService;

	@GetMapping("metadata")
	public HashMap<String, Object> getMetadata() {
		HashMap<String, Object> response = new HashMap<>();
		CaseMetadata metadata = _metadataService.getCaseMetadata();
		ZonedDateTime lastUpdated = null;
		if (metadata != null) {
			lastUpdated = metadata.getLastUpdated();
		}
		response.put("lastUpdated", lastUpdated);
		return response;
	}
}
