package gov.usds.case_issues.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;

/**
 * Model for presenting site navigation information to the client.
 */
public class NavigationEntry {

	private CaseManagementSystem _caseManagementSystem;
	private Iterable<CaseType> _validCaseTypes;

	public NavigationEntry(CaseManagementSystem caseManagementSystem, Iterable<CaseType> validCaseTypes) {
		super();
		_caseManagementSystem = caseManagementSystem;
		_validCaseTypes = validCaseTypes;
	}

	@JsonProperty("tag")
	public String getCaseManagementSystemTag() {
		return _caseManagementSystem.getExternalId();
	}

	public String getName() {
		return _caseManagementSystem.getName();
	}

	public String getDescription() {
		return _caseManagementSystem.getDescription();
	}

	public String getApplicationUrl() {
		return _caseManagementSystem.getApplicationUrl();
	}

	public String getCaseDetailsUrlTemplate() {
		return _caseManagementSystem.getCaseDetailsUrlTemplate();
	}

	public Iterable<CaseType> getCaseTypes() {
		return _validCaseTypes;
	}
}
