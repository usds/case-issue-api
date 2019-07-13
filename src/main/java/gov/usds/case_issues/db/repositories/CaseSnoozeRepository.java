package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.TroubleCase;

public interface CaseSnoozeRepository extends PagingAndSortingRepository<CaseSnooze, Long> {

	/** Retrieve all snoozes for a particular case */
	public Iterable<CaseSnooze> findAllBySnoozeCaseOrderBySnoozeStartAsc(TroubleCase snoozed);
}
