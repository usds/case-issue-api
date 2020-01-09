package gov.usds.case_issues.services;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test for the original (deprecation-bait) implementation of case-list fetching
 */
public class CaseListServicePagingTest extends CaseListPagingFilteringTest {

	@Autowired
	private CaseListService _service;

	@Override
	protected CasePagingService getService() {
		return _service;
	}

}
