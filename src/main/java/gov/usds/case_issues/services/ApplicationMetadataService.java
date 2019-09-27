package gov.usds.case_issues.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.model.NavigationEntry;

/**
 * Service object for retrieving application navigation and presentation information for the front end.
 */
@Service
@Transactional(readOnly=true)
public class ApplicationMetadataService {
	private static final Logger LOG = LoggerFactory.getLogger(ApplicationMetadataService.class);

	@Autowired
	private CaseManagementSystemRepository _systemRepo;
	@Autowired
	private CaseTypeRepository _typeRepo;

	public List<NavigationEntry> getCaseNavigation() {
		LOG.info("Getting all case management systems from {}.", _systemRepo);
		LOG.info("Getting Case types from {}.", _typeRepo);

		Iterable<CaseManagementSystem> allSystems = _systemRepo.findAll(Sort.by("name").ascending());
		Iterable<CaseType> allTypesIterable = _typeRepo.findAll();

		List<NavigationEntry> result = new ArrayList<>();
		allSystems.forEach(sys -> result.add(new NavigationEntry(sys, allTypesIterable)));
		return result;
	}
}
