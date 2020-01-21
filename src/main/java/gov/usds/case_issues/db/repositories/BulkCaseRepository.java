package gov.usds.case_issues.db.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

import org.hibernate.validator.constraints.Length;
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

	@Query(name="snoozedFirstPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCases(
			Long caseManagementSystemId,
			Long caseTypeId,
			@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
			@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		    @Range(max=MAX_PAGE_SIZE) int size);

	@Query(name="snoozeReasonFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCases(
		long caseManagementSystemId,
		long caseTypeId,
		@NotNull @Length(min=1) String snoozeReason,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="snoozeReasonFirstPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCases(
		long caseManagementSystemId,
		long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		@NotNull @Length(min=1) String snoozeReason,
	    @Range(max=MAX_PAGE_SIZE) int size
    );

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

	@Query(name="snoozeReasonLaterPage")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCasesAfter(
		long caseManagementSystemId,
		long caseTypeId,
		ZonedDateTime lastSnoozeEnd,
		ZonedDateTime caseCreation,
		long internalId,
		@NotNull @Length(min=1) String snoozeReason,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="snoozedLaterPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime lastSnoozeEnd,
		ZonedDateTime caseCreation,
		Long internalId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="snoozeReasonLaterPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getSnoozedCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime lastSnoozeEnd,
		ZonedDateTime caseCreation,
		Long internalId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		@NotNull @Length(min=1) String snoozeReason,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="notCurrentlySnoozedFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getActiveCases(
		Long caseManagementSystemId,
		Long caseTypeId,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="notCurrentlySnoozedFirstPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getActiveCases(
		Long caseManagementSystemId,
		Long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
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

	@Query(name="notCurrentlySnoozedLaterPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getActiveCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime caseCreation,
		Long internalId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="previouslySnoozedFirstPage")
	@RestResource(exported=false)
	public List<Object[]> getPreviouslySnoozedCases(
		Long caseManagementSystemId,
		Long caseTypeId,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="previouslySnoozedFirstPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getPreviouslySnoozedCases(
		Long caseManagementSystemId,
		Long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="previouslySnoozedLaterPage")
	@RestResource(exported=false)
	public List<Object[]> getPreviouslySnoozedCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime caseCreation,
		Long internalId,
		@Range(max=MAX_PAGE_SIZE) int size
	);

	@Query(name="previouslySnoozedLaterPageDateFilter")
	@RestResource(exported=false)
	public List<Object[]> getPreviouslySnoozedCasesAfter(
		Long caseManagementSystemId,
		Long caseTypeId,
		ZonedDateTime caseCreation,
		Long internalId,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseCreationWindowEnd,
		@Range(max=MAX_PAGE_SIZE) int size
	);


	@Query(name="resolvedCount")
	@RestResource(exported=false)
	public Integer getResolvedCaseCount(
		Long caseManagementSystemId,
		Long caseTypeId,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowStart,
		@PastOrPresent @NotNull ZonedDateTime caseClosedWindowEnd
	);

	@Query(name="summary")
	@RestResource(exported=false)
	public List<Object[]> getSnoozeSummary(Long caseManagementSystemId, Long caseTypeId);
}
