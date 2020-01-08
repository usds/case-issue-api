package gov.usds.case_issues.db.model.projections;

import java.time.ZonedDateTime;

/**
 * A projection that returns the information about a snooze that most people
 * would want (assuming you already had the key information required to look it
 * up, and don't need to see it again).
 */
public interface CaseSnoozeSummary {

	String getSnoozeReason();

	ZonedDateTime getSnoozeStart();

	ZonedDateTime getSnoozeEnd();
}
