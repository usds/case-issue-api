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

	/** The hour of the day (in server local time) at which all snoozes expire, if they are not manually terminated first */
	public static final int EXPIRES_TIME = 3;

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
	
	protected CaseSnooze() { /* for hibernate/JPA */ }

	public CaseSnooze(TroubleCase troubleCase, String reason, int days) {
		snoozeCase = troubleCase;
		snoozeReason = reason;
		snoozeStart = ZonedDateTime.now();
		snoozeEnd = getEndTime(snoozeStart, days);
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

	public void endSnoozeNow() {
		snoozeEnd = ZonedDateTime.now();
	}

	public static ZonedDateTime getEndTime(ZonedDateTime startTime, int durationDays) {
		return startTime
				.truncatedTo(ChronoUnit.DAYS)
				.withHour(EXPIRES_TIME)
				.plusDays(durationDays);
	}
}
