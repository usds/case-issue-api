package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonView;

import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.model.ApiViews;

@Entity
public class CaseSnooze implements CaseSnoozeSummary {

	@Id
	@GeneratedValue
	private Long caseSnoozeId;

	@ManyToOne(optional=false)
	private TroubleCase snoozeCase;
	@NotNull
	private String snoozeReason; // Needs FK relationship
	@NotNull
	private ZonedDateTime snoozeStart;
	@NotNull
	private ZonedDateTime snoozeEnd;
	private String snoozeDetails; // probably ends up being a couple of FK or a JSON field
	
	protected CaseSnooze() { /* for hibernate/JPA */ }

	public CaseSnooze(TroubleCase troubleCase, String reason, int days) {
		snoozeCase = troubleCase;
		snoozeReason = reason;
		snoozeStart = ZonedDateTime.now();
		snoozeEnd = snoozeStart.plusDays(days);
	}

	public Long getCaseSnoozeId() {
		return caseSnoozeId;
	}
	public TroubleCase getSnoozeCase() {
		return snoozeCase;
	}

	@JsonView(ApiViews.Summary.class)
	public String getSnoozeReason() {
		return snoozeReason;
	}

	public ZonedDateTime getSnoozeStart() {
		return snoozeStart;
	}

	public ZonedDateTime getSnoozeEnd() {
		return snoozeEnd;
	}

	public String getSnoozeDetails() {
		return snoozeDetails;
	}

	public void endSnoozeNow() {
		snoozeEnd = ZonedDateTime.now();
	}
	
}
