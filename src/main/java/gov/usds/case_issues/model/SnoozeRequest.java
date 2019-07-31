package gov.usds.case_issues.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usds.case_issues.db.model.CaseSnooze;

/**
 * API definition for a request from the client to create a new {@link CaseSnooze} object.
 */
public class SnoozeRequest {

	private String snoozeReason;
	private String snoozeDetails;
	private int duration;

	public SnoozeRequest(String snoozeType, String snoozeDetails, int duration) {
		super();
		this.snoozeReason = snoozeType;
		this.snoozeDetails = snoozeDetails;
		this.duration = duration;
	}

	@JsonProperty("reason")
	@Pattern(regexp="\\p{Alpha}[-_\\p{Alnum}}+")
	public String getSnoozeReason() {
		return snoozeReason;
	}

	@JsonProperty("details")
	public String getSnoozeDetails() {
		return snoozeDetails;
	}

	@Min(1)
	@JsonProperty("duration")
	public int getDuration() {
		return duration;
	}
}
