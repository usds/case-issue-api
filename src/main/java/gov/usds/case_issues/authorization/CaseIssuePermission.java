package gov.usds.case_issues.authorization;

import org.springframework.security.core.GrantedAuthority;

/**
 * Granular authorities that can be granted within this application.
 */
public enum CaseIssuePermission implements GrantedAuthority {
	/** The base permission: gives access to read the case lists and case details */
	READ_CASES,
	/** The next most basic: gives access to create and update notes and snoozes */
	UPDATE_CASES,
	/** The site-admin's friend, the ability to directly manipulate objects through the resource APIs */
	UPDATE_STRUCTURE,
	/** The ability to update an issue list using the PUT endpoints (ideally, only held by automated processes) */
	UPDATE_ISSUES,
	;

	@Override
	public String getAuthority() {
		return name();
	}
}
