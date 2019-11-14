package gov.usds.case_issues.db.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.model.TroubleCase;

public interface CaseSnoozeRepository extends PagingAndSortingRepository<CaseSnooze, Long> {

	/** Retrieve all snoozes for a particular case, excluding details that are not exposed to read-only clients */
	public List<CaseSnoozeSummary> findAllBySnoozeCaseOrderBySnoozeStartAsc(TroubleCase snoozed);

	/** Retrieve the latest snooze, if any */
	public Optional<CaseSnooze> findFirstBySnoozeCaseOrderBySnoozeEndDesc(TroubleCase mainCase);
}
