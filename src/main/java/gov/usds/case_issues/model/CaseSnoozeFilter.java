package gov.usds.case_issues.model;

/**
 * Categories of filter that a user of the general case list API might request.
 */
public enum CaseSnoozeFilter {
	/** All cases */
	ALL,
	/** Cases the have been snoozed */
	TRIAGED,
	/** Cases that are not now and have never in the past been snoozed */
	UNCHECKED,
	/** Cases that were snoozed but the snooze has expired */
	ALARMED
}
