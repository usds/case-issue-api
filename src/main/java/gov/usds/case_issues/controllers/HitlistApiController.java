package gov.usds.case_issues.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.ApiViews;

@RestController
@RequestMapping("/api")
public class HitlistApiController {

	private static final Logger LOG = LoggerFactory.getLogger(HitlistApiController.class);

	@Autowired
	private CaseTypeRepository _caseTypeRepo;
	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private TroubleCaseRepository _caseRepo;

	@JsonView(ApiViews.Summary.class)
	@RequestMapping(value = "/cases/{caseManagementSystemTag}/{caseTypeTag}", method = RequestMethod.GET)
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
}
