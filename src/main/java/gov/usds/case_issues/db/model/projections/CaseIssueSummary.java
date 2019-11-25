package gov.usds.case_issues.db.model.projections;

import java.time.ZonedDateTime;

/**
 * A projection that returns the information about an issue that most people would want
 * (assuming you already had the key information required to look it up, and don't need to
 * see it again).
 */
public interface CaseIssueSummary {

	String getIssueType();

	ZonedDateTime getIssueCreated();

	ZonedDateTime getIssueClosed();
}
