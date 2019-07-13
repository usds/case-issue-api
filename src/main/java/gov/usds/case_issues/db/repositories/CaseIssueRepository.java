package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import gov.usds.case_issues.db.model.CaseIssue;

@RepositoryRestResource(path="issues", collectionResourceRel="issues", itemResourceRel="issue")
public interface CaseIssueRepository extends PagingAndSortingRepository<CaseIssue, Long> {

}
