package gov.usds.case_issues.services;

import org.springframework.beans.factory.annotation.Autowired;

public class CriteriaServicePagingTest extends CaseListPagingFilteringTest {

	@Autowired
	private SneakyCaseService _service;

	@Override
	protected CasePagingService getService() {
		return _service;
	}

}
