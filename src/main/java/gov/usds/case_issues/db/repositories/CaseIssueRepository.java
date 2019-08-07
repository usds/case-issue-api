package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;

@RepositoryRestResource(path="issues", collectionResourceRel="issues", itemResourceRel="issue")
public interface CaseIssueRepository extends PagingAndSortingRepository<CaseIssue, Long> {

	List<CaseIssueSummary> findAllByIssueCaseOrderByIssueCreated(TroubleCase mainCase);

	@Query(
		"select i from #{#entityName} i join  fetch i.issueCase "
		+ "where i.issueCase.caseManagementSystem = :caseManagementSystem "
		+ "and i.issueCase.caseType = :caseType "
		+ "and i.issueType = :issueType "
		+ "and i.issueClosed is null "
	)
	// we could force pre-fetching of the case management system and type with @EntityGraph(attributePaths={"issueCase.caseType"})
	// if we wanted to, but that would only save us two queries and only if we had crossed a transaction boundary: probably
	// not worth the trouble
	List<CaseIssue> findActiveIssues(CaseManagementSystem caseManagementSystem, CaseType caseType, String issueType);
}
