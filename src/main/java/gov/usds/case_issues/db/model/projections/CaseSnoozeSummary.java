package gov.usds.case_issues.db.model.projections;

import java.time.ZonedDateTime;

public interface CaseSnoozeSummary {

	String getSnoozeReason();

	ZonedDateTime getSnoozeStart();

	ZonedDateTime getSnoozeEnd();

	String getSnoozeDetails();

}
