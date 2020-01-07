package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;

/**
 * API Model for the full details of a {@link TroubleCase}, including all issues (open and closed)
 * and all snoozes (active and past) and notes.
 */
public class CaseDetails {

	private TroubleCase rootCase;
	private Collection<? extends CaseIssueSummary> issues;
	private Collection<? extends CaseSnoozeSummaryFacade> snoozes;
	private List<AttachmentSummary> notes;

	public CaseDetails(TroubleCase rootCase,
			Collection<? extends CaseIssueSummary> issues,
			Collection<? extends CaseSnoozeSummaryFacade> snoozes,
			List<AttachmentSummary> notes) {
		super();
		this.rootCase = rootCase;
		this.issues = issues;
		this.snoozes = snoozes;
		this.notes = notes;
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

	@JsonSerialize(contentAs=CaseSnoozeSummaryFacade.class)
	public Collection<? extends CaseSnoozeSummaryFacade> getSnoozes() {
		return snoozes;
	}

	public List<AttachmentSummary> getNotes() {
		return notes;
	}
}
