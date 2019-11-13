package gov.usds.case_issues.db.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Repository interface for bulk operations (not independently autowired because
 * this turns out to break things horribly).
 */
public interface BulkCaseRepository {

	@Query(name="snoozedFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCases(Long caseManagementSystemId, Long caseTypeId, Integer size);

	@Query(name="snoozedLaterPage")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime lastSnoozeEnd,
		ZonedDateTime caseCreation,
		Long internalId,
		Integer size
	);

	@Query(name="notCurrentlySnoozedFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getActiveCases(
		Long caseManagementSystemId,
		Long caseTypeId,
		Integer size
	);

	@Query(name="notCurrentlySnoozedLaterPage")
	@RestResource(exported=false)
	public List<Object[]> getActiveCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime caseCreation,
		Long internalId,
		Integer size
	);

	@Query(name="summary")
	@RestResource(exported=false)
	public List<Object[]> getSnoozeSummary(Long caseManagementSystemId, Long caseTypeId);
}
