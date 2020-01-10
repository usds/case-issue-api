package gov.usds.case_issues.services;

import org.springframework.beans.factory.annotation.Autowired;

public class CaseFilteringServiceTest extends CaseListPagingFilteringTest {

	@Autowired
	private CaseFilteringService _service;

	@Override
	protected CasePagingService getService() {
		return _service;
	}
	
}
