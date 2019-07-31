package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.Map;

import gov.usds.case_issues.db.model.TroubleCase;

public class CaseInformation {

	private TroubleCase rootCase;
	private ZonedDateTime lastSnoozeEnd;

	public CaseInformation(TroubleCase rootCase, ZonedDateTime row) {
		super();
		this.rootCase = rootCase;
		this.lastSnoozeEnd = row;
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
}
