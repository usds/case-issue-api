package gov.usds.case_issues.controllers;

import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.services.KPIService;


/**
 * Controller to see information about the current user.
 */
@RestController
@RequestMapping("/api/reporting/{caseManagementSystemTag}/{caseTypeTag}")
public class KPIController {

	@Autowired
	KPIService _KPIService;

	@GetMapping
	public Map<String, Object> getAppPerformanceMetrics(
		@PathVariable String caseManagementSystemTag,
		@PathVariable String caseTypeTag,
		@RequestParam(required=false) @DateTimeFormat(iso=ISO.DATE_TIME) ZonedDateTime start
	) {
		if (start == null) {
			return _KPIService.getKPIData(caseManagementSystemTag, caseTypeTag, ZonedDateTime.now());
		}
		return _KPIService.getKPIData(caseManagementSystemTag, caseTypeTag, start);
	}
}
