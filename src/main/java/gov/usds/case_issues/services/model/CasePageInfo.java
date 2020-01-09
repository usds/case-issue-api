package gov.usds.case_issues.services.model;

import java.time.ZonedDateTime;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

public class CasePageInfo extends CaseGroupInfo {

	private TroubleCase _case;

	public CasePageInfo(CaseGroupInfo group, TroubleCase c) {
		this(group.getCaseManagementSystem(), group.getCaseType(), c);
	}

	public CasePageInfo(CaseManagementSystem system, CaseType type, TroubleCase c) {
		super(system, type);
		_case = c;
	}

	public boolean isFirstPage() {
		return _case == null;
	}

	public TroubleCase getCase() {
		assertCase();
		return _case;
	}

	public Long getCaseId() {
		assertCase();
		return _case.getInternalId();
	}

	public ZonedDateTime getCaseCreationDate() {
		assertCase();
		return _case.getCaseCreation();
	}

	private void assertCase() {
		if (null == _case) {
			throw new IllegalArgumentException("No case was included in this page request");
		}
	}
}