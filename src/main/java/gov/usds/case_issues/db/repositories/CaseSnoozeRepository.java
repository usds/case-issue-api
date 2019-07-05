package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseSnooze;

public interface CaseSnoozeRepository extends PagingAndSortingRepository<CaseSnooze, Long> {

}
