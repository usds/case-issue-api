package gov.usds.case_issues.db.model.projections;

import java.time.ZonedDateTime;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.User;

/**
 * A projection that returns the information about a snooze that most people
 * would want (assuming you already had the key information required to look it
 * up, and don't need to see it again).
 */
public class CaseSnoozeSummary {

	private String snoozeReason;
	private ZonedDateTime snoozeStart;
	private ZonedDateTime snoozeEnd;
	public String userId;
	public String userName;

	public CaseSnoozeSummary(CaseSnooze backEnd) {
		userId = backEnd.getCreatedBy();
		userName = "";
		snoozeReason = backEnd.getSnoozeReason();
		snoozeStart = backEnd.getSnoozeStart();
		snoozeEnd = backEnd.getSnoozeEnd();
	}

	public CaseSnoozeSummary(CaseSnooze backEnd, User user) {
		this(backEnd);
		userName = user.getPrintName();
	}

	public String getSnoozeReason() {
		return snoozeReason;
	};

	public ZonedDateTime getSnoozeStart() {
		return snoozeStart;
	}

	public ZonedDateTime getSnoozeEnd() {
		return snoozeEnd;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

}
