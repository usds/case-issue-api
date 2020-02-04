package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

/**
 * API container for the information we will return about each case in a list of either active
 * or snoozed cases. Exposes summary information about the {@link TroubleCase}, summary information
 * about the most recent CaseSnooze applied to it, and a flag to indicate if this is a
 * previously-snoozed case (a shortcut for checking for snooze information and then checking if the
 * snooze has expired).
 */
public class CaseSummaryImpl implements CaseSummary {

	private TroubleCase rootCase;
	private CaseSnoozeSummary snoozeSummary;
	private List<AttachmentSummary> notes;

	public CaseSummaryImpl(TroubleCase rootCase, CaseSnoozeSummary summary, List<AttachmentSummary> notes) {
		super();
		this.rootCase = rootCase;
		this.snoozeSummary = summary;
		this.notes = notes;
	}

	/* (non-Javadoc)
	 * @see gov.usds.case_issues.model.CaseRequest#getReceiptNumber()
	 */
	@Override
	public String getReceiptNumber() {
		return rootCase.getReceiptNumber();
	}

	/* (non-Javadoc)
	 * @see gov.usds.case_issues.model.CaseRequest#getCaseCreation()
	 */
	@Override
	public ZonedDateTime getCaseCreation() {
		return rootCase.getCaseCreation();
	}

	/* (non-Javadoc)
	 * @see gov.usds.case_issues.model.CaseRequest#getExtraData()
	 */
	@Override
	public Map<String, Object> getExtraData() {
		return rootCase.getExtraData();
	}

	public boolean isPreviouslySnoozed() {
		return snoozeSummary != null && (
			snoozeSummary.getSnoozeEnd().isBefore(ZonedDateTime.now()) ||
			snoozeSummary.getSnoozeResolved() != null
		);
	}

	@JsonSerialize(as=CaseSnoozeSummary.class)
	public CaseSnoozeSummary getSnoozeInformation() {
		return snoozeSummary;
	}

	public List<AttachmentSummary> getNotes() {
		return notes;
	}
}
