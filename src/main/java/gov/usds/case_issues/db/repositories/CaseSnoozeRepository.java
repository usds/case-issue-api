package gov.usds.case_issues.db.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.TroubleCase;

public interface CaseSnoozeRepository extends PagingAndSortingRepository<CaseSnooze, Long> {

	/** Find all cases with the given IDs. Overrides the inherited method that inexplicably returns an Iterable. */
	@Override
	public Collection<CaseSnooze> findAllById(Iterable<Long> ids);

	/** Retrieve all snoozes for a particular case, excluding details that are not exposed to read-only clients */
	public List<CaseSnooze> findAllBySnoozeCaseOrderBySnoozeStartAsc(TroubleCase snoozed);

	/** Retrieve the latest snooze, if any */
	public Optional<CaseSnooze> findFirstBySnoozeCaseOrderBySnoozeEndDesc(TroubleCase mainCase);
}
