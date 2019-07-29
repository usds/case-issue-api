package gov.usds.case_issues.db.model.projections;

import java.time.ZonedDateTime;

public interface CaseIssueSummary {

	String getIssueType();

	ZonedDateTime getIssueCreated();

	ZonedDateTime getIssueClosed();

}
