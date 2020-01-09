package gov.usds.case_issues.services.model;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;

public class CaseGroupInfo {

	private CaseManagementSystem _system;
	private CaseType _type;

	public CaseGroupInfo(CaseManagementSystem _system, CaseType _type) {
		super();
		this._system = _system;
		this._type = _type;
	}

	public Long getCaseManagementSystemId() {
		return _system.getInternalId();
	}

	public Long getCaseTypeId() {
		return _type.getInternalId();
	}

	public CaseManagementSystem getCaseManagementSystem() {
		return _system;
	}

	public CaseType getCaseType() {
		return _type;
	}
}