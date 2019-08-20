package gov.usds.case_issues.db.repositories;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseManagementSystem;

/**
 * Repository for finding and (rarely) creating and modifying {@link CaseManagementSystem} entities.
 */
public interface CaseManagementSystemRepository extends PagingAndSortingRepository<CaseManagementSystem, Long> {

	/** Find the system based on the URL-safe tag (e.g. "CM1K") */
	public Optional<CaseManagementSystem> findByCaseManagementSystemTag(String tag);
	/** Find the system based on its proper name (e.g. "Case Manager 1000") */
	public Optional<CaseManagementSystem> findByName(String name);

}
