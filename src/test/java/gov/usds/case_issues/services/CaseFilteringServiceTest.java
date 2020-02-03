package gov.usds.case_issues.services;

import static gov.usds.case_issues.test_util.CaseListFixtureService.CASE_TYPE;
import static gov.usds.case_issues.test_util.CaseListFixtureService.SYSTEM;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.services.model.CaseFilter;
import gov.usds.case_issues.test_util.CaseListFixtureService;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureAttachment;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureCase;

@SuppressWarnings("checkstyle:MagicNumber")
public class CaseFilteringServiceTest extends CaseListPagingFilteringTest {

	@Autowired
	private CaseFilteringService _service;

	@Override
	protected CasePagingService getService() {
		return _service;
	}

	@Test
	public void getCases_uncheckedCasesFirstPage_correctResult() {
		List<? extends CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.UNCHECKED), 3,
				Optional.empty(), Optional.empty(), Collections.emptyList()).getCases();
		assertCaseOrder("active only!", Arrays.asList(FixtureCase.ACTIVE01, FixtureCase.ACTIVE04, FixtureCase.ACTIVE02), foundCases);
	}

	@Test
	public void getCases_uncheckedCasesParityEven_correctResult() {
		List<CaseFilter> filters = Collections.singletonList(
				FilterFactory.caseExtraData(Collections.singletonMap(CaseListFixtureService.Keywords.PARITY, CaseListFixtureService.Keywords.EVEN))
		);
		List<? extends CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.UNCHECKED), 3, Optional.empty(), Optional.empty(), filters).getCases();
		assertCaseOrder("active and even", Arrays.asList(FixtureCase.ACTIVE03, FixtureCase.ACTIVE05), foundCases);
	}

	@Test
	public void getCases_snoozedCasesCorrelationIdAttached_correctResult() {
		List<CaseFilter> filters = Collections.singletonList(FilterFactory.hasAttachment(FixtureAttachment.CORRELATION01.asRequest()));
		List<? extends CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED), 3, Optional.empty(), Optional.empty(), filters).getCases();
		assertCaseOrder("Snoozed with correlation ID", Arrays.asList(FixtureCase.SNOOZED02, FixtureCase.SNOOZED01), foundCases);
	}

	@Test
	public void getCases_snoozedCasesAnyTroubleLink_correctResult() {
		List<CaseFilter> filters = Collections.singletonList(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.LINK, null, "trouble")));
		List<? extends CaseSummary> foundCases = _service.getCases(
				SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED), 5, Optional.of(CaseFilteringService.DEFAULT_SORT), Optional.empty(), filters).getCases();
		assertCaseOrder("Snoozed with a trouble link", Arrays.asList(FixtureCase.SNOOZED02, FixtureCase.SNOOZED03, FixtureCase.SNOOZED04), foundCases);
	}

}
