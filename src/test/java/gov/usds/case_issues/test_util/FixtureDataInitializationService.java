package gov.usds.case_issues.test_util;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;

@Service
@Transactional(readOnly=false)
public class FixtureDataInitializationService {

	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private CaseTypeRepository _caseTypeRepository;

	public CaseManagementSystem ensureCaseManagementSystemInitialized(String tag, String name, String description) {
		Optional<CaseManagementSystem> found = _caseManagementSystemRepo.findByCaseManagementSystemTag(tag);
		if (found.isPresent()) {
			return found.get();
		}
		return _caseManagementSystemRepo.save(new CaseManagementSystem(tag, name, description));
	}

	public CaseType ensureCaseTypeInitialized(String tag, String name, String description) {
		Optional<CaseType> found = _caseTypeRepository.findByCaseTypeTag(tag);
		if (found.isPresent()) {
			return found.get();
		}
		return _caseTypeRepository.save(new CaseType(tag, name, description));
	}

}
