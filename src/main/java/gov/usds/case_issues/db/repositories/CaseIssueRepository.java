package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;

@RepositoryRestResource(path="issues", collectionResourceRel="issues", itemResourceRel="issue")
public interface CaseIssueRepository extends PagingAndSortingRepository<CaseIssue, Long> {

	List<CaseIssueSummary> findAllByIssueCaseOrderByIssueCreated(TroubleCase mainCase);

}
