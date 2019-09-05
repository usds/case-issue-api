package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseManagementSystem;

/**
 * Repository for finding and (rarely) creating and modifying {@link CaseManagementSystem} entities.
 */
public interface CaseManagementSystemRepository extends TaggedEntityRepository<CaseManagementSystem>, PagingAndSortingRepository<CaseManagementSystem, Long> {

}
