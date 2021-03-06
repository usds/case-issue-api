package gov.usds.case_issues.db.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.validation.annotation.Validated;

/**
 * Repository interface for bulk operations (not independently autowired because
 * this turns out to break things horribly).
 */
@Validated
public interface BulkCaseRepository {

	@Query(name="resolvedCount")
	@RestResource(exported=false)
	public int getResolvedCaseCount(
		Long caseManagementSystemId,
		Long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowEnd
	);

	@Query(name="averageDaysToResoluton")
	@RestResource(exported=false)
	public int getAverageDaysToResolution(
		Long caseManagementSystemId,
		Long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowEnd
	);


	@Query(name="averageDaysWorked")
	@RestResource(exported=false)
	public int getAverageDaysWorked(
		Long caseManagementSystemId,
		Long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowEnd
	);

	@Query(name="summary")
	@RestResource(exported=false)
	public List<Object[]> getSnoozeSummary(Long caseManagementSystemId, Long caseTypeId);
}
