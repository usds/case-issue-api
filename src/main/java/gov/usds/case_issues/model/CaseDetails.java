package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

/**
 * API Model for the full details of a {@link TroubleCase}.
 */
public class CaseDetails {

	private TroubleCase rootCase;
	private Collection<? extends CaseIssueSummary> issues;
	private Collection<? extends CaseSnoozeSummary> snoozes;

	public CaseDetails(TroubleCase rootCase, Collection<? extends CaseIssueSummary> issues, Collection<? extends CaseSnoozeSummary> snoozes) {
		super();
		this.rootCase = rootCase;
		this.issues = issues;
		this.snoozes = snoozes;
	}

	public CaseManagementSystem getCaseManagementSystem() {
		return rootCase.getCaseManagementSystem();
	}
	public String getReceiptNumber() {
		return rootCase.getReceiptNumber();
	}
	public CaseType getCaseType() {
		return rootCase.getCaseType();
	}
	public ZonedDateTime getCaseCreation() {
		return rootCase.getCaseCreation();
	}
	public Map<String, Object> getExtraData() {
		return rootCase.getExtraData();
	}

	@JsonSerialize(contentAs=CaseIssueSummary.class)
	public Collection<? extends CaseIssueSummary> getIssues() {
		return issues;
	}

	@JsonSerialize(contentAs=CaseSnoozeSummary.class)
	public Collection<? extends CaseSnoozeSummary> getSnoozes() {
		return snoozes;
	}
}
