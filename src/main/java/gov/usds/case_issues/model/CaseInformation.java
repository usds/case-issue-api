package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

public class CaseInformation {

	private TroubleCase rootCase;
	private ZonedDateTime lastSnoozeEnd;
	private CaseSnoozeSummary snoozeSummary;

	public CaseInformation(TroubleCase rootCase, ZonedDateTime row, CaseSnoozeSummary summary) {
		super();
		this.rootCase = rootCase;
		this.lastSnoozeEnd = row;
		this.snoozeSummary = summary;
	}

	public String getReceiptNumber() {
		return rootCase.getReceiptNumber();
	}

	public ZonedDateTime getCaseCreation() {
		return rootCase.getCaseCreation();
	}

	public Map<String, Object> getExtraData() {
		return rootCase.getExtraData();
	}

	public boolean isPreviouslySnoozed() {
		return lastSnoozeEnd != null && lastSnoozeEnd.isBefore(ZonedDateTime.now());
	}

	@JsonSerialize(as=CaseSnoozeSummary.class)
	public CaseSnoozeSummary getSnoozeInformation() {
		return snoozeSummary;
	}
}
