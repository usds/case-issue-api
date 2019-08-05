package gov.usds.case_issues.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.services.CaseListService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/cases/{caseManagementSystemTag}/{caseTypeTag}")
@CrossOrigin("http://localhost:3000")
public class HitlistApiController {

	@Autowired
	private CaseListService _listService;

	@GetMapping("snoozed")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.", defaultValue = "20"),
	})
	public List<CaseSummary> getSnoozedCases(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @ApiIgnore Pageable pageMe) {
		return _listService.getSnoozedCases(caseManagementSystemTag, caseTypeTag, pageMe);
	}

	@GetMapping("active")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
				value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.", defaultValue = "20"),
	})
	public List<CaseSummary>  getActiveCases(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @ApiIgnore Pageable pageMe) {
		return _listService.getActiveCases(caseManagementSystemTag, caseTypeTag, pageMe);
	}

	@RequestMapping(value="summary", method=RequestMethod.GET)
	public Map<String, Number> getSummary(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag) {
		return _listService.getSummaryInfo(caseManagementSystemTag, caseTypeTag);
	}
}
