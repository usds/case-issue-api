package gov.usds.case_issues.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.services.KPIService;


/**
 * Controller to see information about the current user.
 */
@RestController
@Profile("auth-testing")
@RequestMapping("/kpis/{caseManagementSystemTag}/{caseTypeTag}")
public class KPIController {

	@Autowired
	KPIService _KPIService;

	@GetMapping
	public Map<String, Object> getCurrentUser(
		@PathVariable String caseManagementSystemTag,
		@PathVariable String caseTypeTag
	) {
		return _KPIService.getKPIData(caseManagementSystemTag, caseTypeTag);
	}


}
