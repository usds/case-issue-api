package gov.usds.case_issues.db.model.reporting;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UpdatableEntity;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.model.CaseRequest;

@Entity
@Immutable
@Table(name="sneaky_view")

public class SneakyViewEntity extends UpdatableEntity implements CaseSnoozeSummary, CaseRequest {

	@ManyToOne(optional=false)
	@JoinColumn(nullable=false)
	private CaseManagementSystem caseManagementSystem;

	@NaturalId
	@NotNull
	private String receiptNumber;

	@ManyToOne(optional=false)
	@JoinColumn(nullable=false)
	private CaseType caseType;

	private ZonedDateTime caseCreation;
	@Column(name="extra_data_binary")
	private Map<String, Object> extraData;

	// snooze embeds
	private String snoozeReason;
	private ZonedDateTime snoozeStart;
	private ZonedDateTime snoozeEnd;

	// state
	private boolean hasOpenIssue;
	
	public CaseManagementSystem getCaseManagementSystem() {
		return caseManagementSystem;
	}


	public CaseType getCaseType() {
		return caseType;
	}


	public String getReceiptNumber() {
		return receiptNumber;
	}


	@Override
	public ZonedDateTime getCaseCreation() {
		return caseCreation;
	}


	@Override
	public Map<String, Object> getExtraData() {
		return extraData;
	}

	public boolean isHasOpenIssue() {
		return hasOpenIssue;
	}

	@Override
	public String getSnoozeReason() {
		return snoozeReason;
	}


	@Override
	public ZonedDateTime getSnoozeStart() {
		return snoozeStart;
	}


	@Override
	public ZonedDateTime getSnoozeEnd() {
		return snoozeEnd;
	}
}
