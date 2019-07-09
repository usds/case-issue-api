package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

public interface TroubleCaseRepository extends PagingAndSortingRepository<TroubleCase, Long> {

	public Iterable<TroubleCase> getAllByCaseManagementSystemAndCaseType(CaseManagementSystem cms, CaseType type);

}
