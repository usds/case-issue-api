package gov.usds.case_issues.services.model;

import java.time.ZonedDateTime;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.reporting.FilterableCase;

public class CasePageInfo extends CaseGroupInfo {

	private FilterableCase _case;

	public CasePageInfo(CaseGroupInfo group, FilterableCase c) {
		this(group.getCaseManagementSystem(), group.getCaseType(), c);
	}

	public CasePageInfo(CaseManagementSystem system, CaseType type, FilterableCase c) {
		super(system, type);
		_case = c;
	}

	public boolean isFirstPage() {
		return _case == null;
	}

	public FilterableCase getCase() {
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