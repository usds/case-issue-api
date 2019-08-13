package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Repository interface for bulk operations (not independently autowired because
 * this turns out to break things horribly).
 */
public interface BulkCaseRepository {

	@Query(name="snoozed")
	@RestResource(exported=false)
	public Page<Object[]> getSnoozedCases(Long caseManagementSystemId, Long caseTypeId, Pageable p);

	@Query(name="unSnoozed")
	@RestResource(exported=false)
	public Page<Object[]> getActiveCases(Long caseManagementSystemId, Long caseTypeId, Pageable p);

	@Query(name="summary")
	@RestResource(exported=false)
	public List<Object[]> getSnoozeSummary(Long caseManagementSystemId, Long caseTypeId);
}
