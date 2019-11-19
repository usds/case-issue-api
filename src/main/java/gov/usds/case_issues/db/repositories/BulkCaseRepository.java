package gov.usds.case_issues.db.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.validation.annotation.Validated;

/**
 * Repository interface for bulk operations (not independently autowired because
 * this turns out to break things horribly).
 */
@Validated
public interface BulkCaseRepository {

	/** The maximum allowed page size for a paged request. */
	public static final int MAX_PAGE_SIZE = 100;

	@Query(name="snoozedFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCases(Long caseManagementSystemId, Long caseTypeId, @Range(max=MAX_PAGE_SIZE)  int size);

	@Query(name="snoozedLaterPage")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime lastSnoozeEnd,
		ZonedDateTime caseCreation,
		Long internalId,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="notCurrentlySnoozedFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getActiveCases(
		Long caseManagementSystemId,
		Long caseTypeId,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="notCurrentlySnoozedLaterPage")
	@RestResource(exported=false)
	public List<Object[]> getActiveCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime caseCreation,
		Long internalId,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="summary")
	@RestResource(exported=false)
	public List<Object[]> getSnoozeSummary(Long caseManagementSystemId, Long caseTypeId);
}
