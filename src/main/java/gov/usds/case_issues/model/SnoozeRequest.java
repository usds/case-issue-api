package gov.usds.case_issues.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usds.case_issues.db.model.CaseSnooze;

/**
 * API definition for a request from the client to create a new {@link CaseSnooze} object.
 */
public class SnoozeRequest {

	private String snoozeReason;
	private int duration;
	private List<AttachmentRequest> notes = new ArrayList<>();

	public SnoozeRequest(String snoozeType, int duration) {
		super();
		this.snoozeReason = snoozeType;
		this.duration = duration;
	}

	@JsonProperty("reason")
	@Pattern(regexp="\\p{Alpha}[-_\\p{Alnum}]+")
	public String getSnoozeReason() {
		return snoozeReason;
	}

	@Min(1)
	@JsonProperty("duration")
	public int getDuration() {
		return duration;
	}

	public List<AttachmentRequest> getNotes() {
		return notes;
	}
}
