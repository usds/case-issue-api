package gov.usds.case_issues.services;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.test_util.CaseListFixtureService;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureAttachment;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureCase;

import static gov.usds.case_issues.test_util.CaseListFixtureService.SYSTEM;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static gov.usds.case_issues.test_util.CaseListFixtureService.CASE_TYPE;

public class CriteriaServicePagingTest extends CaseListPagingFilteringTest {

	@Autowired
	private SneakyCaseService _service;

	@Override
	protected CasePagingService getService() {
		return _service;
	}

	@Test
	public void getCases_uncheckedCasesFirstPage_correctResult() {
		List<CaseSummary> foundCases = _service.getCases(CaseSnoozeFilter.UNCHECKED, SYSTEM, CASE_TYPE, null, Optional.ofNullable(null), 3, Optional.ofNullable(null), 
				Optional.ofNullable(null), Collections.emptyMap(), Optional.ofNullable(null));
		assertCaseOrder("active only!", Arrays.asList(FixtureCase.ACTIVE01, FixtureCase.ACTIVE04, FixtureCase.ACTIVE02), foundCases);
	}

	@Test
	public void getCases_uncheckedCasesParityEven_correctResult() {
		List<CaseSummary> foundCases = _service.getCases(CaseSnoozeFilter.UNCHECKED, SYSTEM, CASE_TYPE, null, Optional.ofNullable(null), 3, Optional.ofNullable(null), 
				Optional.ofNullable(null), Collections.singletonMap(CaseListFixtureService.Keywords.PARITY, CaseListFixtureService.Keywords.EVEN),
				Optional.ofNullable(null));
		assertCaseOrder("active and even", Arrays.asList(FixtureCase.ACTIVE03, FixtureCase.ACTIVE05), foundCases);
	}

	@Test
	public void getCases_snoozedCasesCorrelationIdAttached_correctResult() {
		List<CaseSummary> foundCases = _service.getCases(CaseSnoozeFilter.SNOOZED, SYSTEM, CASE_TYPE, null, Optional.ofNullable(null), 3, Optional.ofNullable(null), 
			Optional.ofNullable(null), Collections.emptyMap(), Optional.of(FixtureAttachment.CORRELATION01.asRequest()));
		assertCaseOrder("Snoozed with correlation ID", Arrays.asList(FixtureCase.SNOOZED02, FixtureCase.SNOOZED01), foundCases);
	}

	@Test
	public void getCases_snoozedCasesAnyTroubleLink_correctResult() {
		List<CaseSummary> foundCases = _service.getCases(CaseSnoozeFilter.SNOOZED, SYSTEM, CASE_TYPE, null, Optional.ofNullable(null), 5, Optional.ofNullable(null), 
				Optional.ofNullable(null), Collections.emptyMap(), Optional.of(new AttachmentRequest(AttachmentType.LINK, null, "trouble")));
		assertCaseOrder("Snoozed with a trouble link", Arrays.asList(FixtureCase.SNOOZED02, FixtureCase.SNOOZED03, FixtureCase.SNOOZED04), foundCases);
	}
}
