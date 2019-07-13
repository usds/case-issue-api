package gov.usds.case_issues.db.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

@RepositoryRestResource(
	path="cases",
	itemResourceRel="case",
	collectionResourceRel="cases",
	collectionResourceDescription=@Description("All cases that have had at least one issue reported.")
)
public interface TroubleCaseRepository extends PagingAndSortingRepository<TroubleCase, Long> {

	public Page<TroubleCase> getAllByCaseManagementSystemAndCaseType(CaseManagementSystem caseManager, CaseType caseType, Pageable pageable);

}
