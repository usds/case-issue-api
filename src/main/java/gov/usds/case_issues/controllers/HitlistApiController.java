package gov.usds.case_issues.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.ApiViews;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/cases/{caseManagementSystemTag}/{caseTypeTag}")
public class HitlistApiController {

	private static final Logger LOG = LoggerFactory.getLogger(HitlistApiController.class);

	@Autowired
	private CaseTypeRepository _caseTypeRepo;
	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private BulkCaseRepository _bulkRepo;

	@JsonView(ApiViews.Summary.class)
	@GetMapping
	public Page<TroubleCase> getAllCases(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, Pageable pageMe) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByCaseTypeTag(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		Page<TroubleCase> cases = _caseRepo.getWithOpenIssues(caseManagementSystem, caseType, pageMe);
		LOG.info("Searching [{}] for cases of type [{}] found {} total cases, {} in this page",
				caseManagementSystem.getCaseManagementSystemTag(), caseType.getCaseTypeTag(),
				cases.getTotalElements(), cases.getNumberOfElements());
		return cases;
	}

	@JsonView(ApiViews.Summary.class)
	@GetMapping("snoozed")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.", defaultValue = "5"),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
					"Default sort order is ascending. " +
					"Multiple sort criteria are supported.")
	})
	public Object doSillyTest(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @ApiIgnore Pageable pageMe) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByCaseTypeTag(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		return _bulkRepo.getSnoozedCases(caseManagementSystem.getCaseManagementSystemId(), caseType.getCaseTypeId(), pageMe);
	}

	@JsonView(ApiViews.Summary.class)
	@GetMapping("active")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
				value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.", defaultValue = "5"),
		@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
			value = "Sorting criteria in the format: property(,asc|desc). " +
					"Default sort order is ascending. " +
					"Multiple sort criteria are supported.")
	})
	public Object doSillierTest(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @ApiIgnore Pageable pageMe) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByCaseTypeTag(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		return _bulkRepo.getActiveCases(caseManagementSystem.getCaseManagementSystemId(), caseType.getCaseTypeId(), pageMe);
	}

	@RequestMapping(value="summary", method=RequestMethod.GET)
	public Map<?, ?> getSummary(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByCaseTypeTag(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		return _bulkRepo.getSnoozeSummary(caseManagementSystem.getCaseManagementSystemId(), caseType.getCaseTypeId()).stream().collect(Collectors.toMap(a->((String) a[0]).trim(), a->a[1]));
	}
}
