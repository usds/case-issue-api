package gov.usds.case_issues.model;

import java.util.List;

import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

public interface CaseSummary extends CaseRequest {

	boolean isPreviouslySnoozed();

	CaseSnoozeSummary getSnoozeInformation();

	List<NoteSummary> getNotes();
}