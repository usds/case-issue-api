package gov.usds.case_issues.model;

import java.util.List;

import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

/**
 * API container for the information we will return about each case in a list of either active
 * or snoozed cases. Exposes summary information about the {@link TroubleCase}, summary information
 * about the most recent CaseSnooze applied to it, and a flag to indicate if this is a
 * previously-snoozed case (a shortcut for checking for snooze information and then checking if the
 * snooze has expired).
 */
public interface CaseSummary extends CaseRequest {

	boolean isPreviouslySnoozed();

	CaseSnoozeSummary getSnoozeInformation();

	List<AttachmentSummary> getNotes();
}
