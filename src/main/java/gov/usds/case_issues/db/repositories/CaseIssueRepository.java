package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseIssue;

public interface CaseIssueRepository extends PagingAndSortingRepository<CaseIssue, Long> {

}
