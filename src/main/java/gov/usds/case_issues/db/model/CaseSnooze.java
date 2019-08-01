package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;

import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

@Entity
@DynamicUpdate
public class CaseSnooze implements CaseSnoozeSummary {

	@Id
	@GeneratedValue
	private Long caseSnoozeId;

	@ManyToOne(optional=false)
	@JoinColumn(updatable=false)
	private TroubleCase snoozeCase;
	@NotNull
	@Column(updatable=false)
	private String snoozeReason; // Needs FK relationship
	@NotNull
	@Column(updatable=false)
	private ZonedDateTime snoozeStart;
	@NotNull
	private ZonedDateTime snoozeEnd;
	private String snoozeDetails; // probably ends up being a couple of FK or a JSON field
	
	protected CaseSnooze() { /* for hibernate/JPA */ }

	public CaseSnooze(TroubleCase troubleCase, String reason, int days) {
		snoozeCase = troubleCase;
		snoozeReason = reason;
		snoozeStart = ZonedDateTime.now();
		snoozeEnd = snoozeStart.truncatedTo(ChronoUnit.DAYS).plusHours(3).plusDays(days);
	}

	public CaseSnooze(TroubleCase troubleCase, String reason, int days, String details) {
		this(troubleCase, reason, days);
		this.snoozeDetails = details;
	}

	public Long getCaseSnoozeId() {
		return caseSnoozeId;
	}
	public TroubleCase getSnoozeCase() {
		return snoozeCase;
	}

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
