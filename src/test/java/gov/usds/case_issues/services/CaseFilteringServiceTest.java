package gov.usds.case_issues.services;

import static gov.usds.case_issues.test_util.CaseListFixtureService.ALTERNATE_SNOOZE_REASON;
import static gov.usds.case_issues.test_util.CaseListFixtureService.CASE_TYPE;
import static gov.usds.case_issues.test_util.CaseListFixtureService.DEFAULT_SNOOZE_REASON;
import static gov.usds.case_issues.test_util.CaseListFixtureService.START_DATE;
import static gov.usds.case_issues.test_util.CaseListFixtureService.SYSTEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.services.model.CaseFilter;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.test_util.CaseListFixtureService;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureAttachment;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureCase;

@SuppressWarnings("checkstyle:MagicNumber")
public class CaseFilteringServiceTest extends CaseIssueApiTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(CaseFilteringServiceTest.class);

	private static final int PAGE_SIZE = 3;
	private static final int ALL_CASES = FixtureCase.values().length;

	/** The date range from 1 to 6 days after our start time */
	private static final DateRange RANGE_MIDDLE = new DateRange(START_DATE.plusDays(1), START_DATE.plusDays(6));
	/** The date range from prehistory to exactly 1 day after our start time. */
	private static final DateRange RANGE_EARLY = new DateRange(START_DATE.minusYears(1), START_DATE.plusDays(1));
	/** The date range from "a long time ago" to the day before our earliest case. */
	private static final DateRange RANGE_BEFORE_TIME = new DateRange(START_DATE.minusYears(1), START_DATE.minusDays(1));


	@Autowired
	private CaseFilteringService _service;
	@Autowired
	private CaseListFixtureService _fixtureService;

	@Before
	public void initPageableData() {
		_fixtureService.initFixtures();
	}

	@Test
	public void getCases_uncheckedCasesFirstPage_correctResult() {
		List<CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.UNCHECKED), 3,
				Optional.empty(), Optional.empty(), Collections.emptyList());
		assertCaseOrder("active only!", Arrays.asList(FixtureCase.ACTIVE01, FixtureCase.ACTIVE04, FixtureCase.ACTIVE02), foundCases);
	}

	@Test
	public void getCases_uncheckedCasesParityEven_correctResult() {
		List<CaseFilter> filters = Collections.singletonList(
				FilterFactory.caseExtraData(Collections.singletonMap(CaseListFixtureService.Keywords.PARITY, CaseListFixtureService.Keywords.EVEN))
		);
		List<CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.UNCHECKED), 3, Optional.empty(), Optional.empty(), filters);
		assertCaseOrder("active and even", Arrays.asList(FixtureCase.ACTIVE03, FixtureCase.ACTIVE05), foundCases);
	}

	@Test
	public void getCases_snoozedCasesCorrelationIdAttached_correctResult() {
		List<CaseFilter> filters = Collections.singletonList(FilterFactory.hasAttachment(FixtureAttachment.CORRELATION01.asRequest()));
		List<CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED), 3, Optional.empty(), Optional.empty(), filters);
		assertCaseOrder("Snoozed with correlation ID", Arrays.asList(FixtureCase.SNOOZED02, FixtureCase.SNOOZED01), foundCases);
	}

	@Test
	public void getCases_snoozedCasesAnyTroubleLink_correctResult() {
		List<CaseFilter> filters = Collections.singletonList(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.LINK, null, "trouble")));
		List<CaseSummary> foundCases = _service.getCases(
				SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED), 5, Optional.of(CaseFilteringService.DEFAULT_SORT), Optional.empty(), filters);
		assertCaseOrder("Snoozed with a trouble link", Arrays.asList(FixtureCase.SNOOZED02, FixtureCase.SNOOZED03, FixtureCase.SNOOZED04), foundCases);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_zeroPageSize_exception() {
		_service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.ACTIVE),
				0, Optional.empty(), Optional.empty(), Collections.emptyList());
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_invalidSystemTag_exception() {
		wrapInvalidCall("hello\nworld", CASE_TYPE, null, 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_invalidTypeTag_exception() {
		wrapInvalidCall(SYSTEM, "hello\nworld", null, 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_invalidReceipt_exception() {
		wrapInvalidCall(SYSTEM, CASE_TYPE, "/etc/passwd", 1);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_excessivePageSize_exception() {
		wrapInvalidCall(SYSTEM, CASE_TYPE, null, 101);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_excessivePageSizeSecondPage_exception() {
		wrapInvalidCall(SYSTEM, CASE_TYPE, "ABCDE", 101);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getCases_negativePageSize_exception() {
		wrapInvalidCall(SYSTEM, CASE_TYPE, null, -10);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_negativePageSizeSecondPage_exception() {
		wrapInvalidCall(SYSTEM, CASE_TYPE, "ABCDE", -10);
	}

	private void wrapInvalidCall(String sys, String caseType, String pageRef, int pageSize) {
		_service.getCases(sys, caseType, Collections.singleton(CaseSnoozeFilter.ACTIVE), pageSize,
			Optional.empty(), Optional.ofNullable(pageRef), Collections.emptyList());
	}

	/* wrapper functions to make it easy to convert tests from the old API */
	@SafeVarargs
	private final List<? extends CaseSummary> wrapCaseFetch(CaseSnoozeFilter f, CaseFilter... filters) {
		return wrapCaseFetch(f, null, PAGE_SIZE, filters);
	}

	@SafeVarargs
	private final List<? extends CaseSummary> wrapCaseFetch(CaseSnoozeFilter f, FixtureCase pageRef, CaseFilter... filters) {
		return wrapCaseFetch(f, pageRef, PAGE_SIZE, filters);
	}

	@SafeVarargs
	private final List<? extends CaseSummary> wrapCaseFetch(CaseSnoozeFilter f, FixtureCase pageRef, int pageSize,
			CaseFilter... filters) {
		return _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(f), pageSize,
				Optional.empty(), Optional.ofNullable(null != pageRef ? pageRef.name() : null), Arrays.asList(filters));
	}

	@Test
	public void getActiveCases_firstPage_correctResult() {
		List<? extends CaseSummary> activeCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE); 
		assertEquals(PAGE_SIZE, activeCases.size());
		assertCaseOrder(activeCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
	}

	@Test
	public void getActiveCases_firstPage_correctAttachments() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
		assertEquals("Attachment count for ACTIVE01", 0, foundCases.get(0).getNotes().size());
		assertEquals("Attachment count for DESNOOZED02", 1, foundCases.get(1).getNotes().size());
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.CORRELATION01, null);
		assertEquals("Attachment count for DESNOOZED01", 3, foundCases.get(2).getNotes().size());
		assertAttachmentPresent(foundCases.get(2), FixtureAttachment.CORRELATION01, null);
		assertAttachmentPresent(foundCases.get(2), FixtureAttachment.LINK01, "https://trouble.gov/?ticket=LINK01");
		assertAttachmentPresent(foundCases.get(2), FixtureAttachment.TAG_BLUE, null);
	}

	@Test
	public void getActiveCases_secondPage_correctResult() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE, FixtureCase.DESNOOZED01);
		assertEquals(PAGE_SIZE, foundCases.size());
		assertCaseOrder(foundCases, FixtureCase.ACTIVE04, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03);
	}

	@Test
	public void getActiveCases_walkThroughCaseList_correctResults() {
		List<FixtureCase> allFixtures = new ArrayList<>(Arrays.asList(
			FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01,
			FixtureCase.ACTIVE04, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03,
			FixtureCase.DESNOOZED03, FixtureCase.ACTIVE05, FixtureCase.DESNOOZED04
		));
		assertCaseOrder(
			"all cases",
			allFixtures,
			wrapCaseFetch(CaseSnoozeFilter.ACTIVE, null, ALL_CASES)
		);
		while (!allFixtures.isEmpty()) {
			FixtureCase firstReceipt = allFixtures.remove(0);
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				wrapCaseFetch(CaseSnoozeFilter.ACTIVE, firstReceipt, ALL_CASES));
		}
	}

	@Test
	public void getActiveCases_firstPageEmptyDateRange_noCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE, FilterFactory.dateRange(RANGE_BEFORE_TIME));
		assertEquals(0, foundCases.size());
	}

	@Test
	public void getActiveCases_firstPageEarlyDateRange_twoCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE, FilterFactory.dateRange(RANGE_EARLY));
		assertCaseOrder(foundCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02);
	}

	@Test
	public void getActiveCases_nextPageEarlyDateRange_noCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE, FixtureCase.DESNOOZED02, FilterFactory.dateRange(RANGE_EARLY));
		assertCaseOrder(foundCases);
	}

	@Test
	public void getActiveCases_firstPageMiddleDateRange_correctCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE,FilterFactory.dateRange(RANGE_MIDDLE));
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.ACTIVE04);
	}

	@Test
	public void getActiveCases_secondPageMiddleDateRange_correctCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE, FixtureCase.ACTIVE04, FilterFactory.dateRange(RANGE_MIDDLE));
		assertCaseOrder(foundCases, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03, FixtureCase.DESNOOZED03);
	}

	@Test
	public void getActiveCases_lastPageMiddleDateRange_correctCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ACTIVE, FixtureCase.DESNOOZED03, FilterFactory.dateRange(RANGE_MIDDLE));
		assertCaseOrder(foundCases, FixtureCase.ACTIVE05);
	}

	@Test
	public void getSnoozedCases_fetchFirstPage_correctResult() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.SNOOZED);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
	}

	@Test
	public void getSnoozedCases_fetchFirstPage_correctAttachments() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.SNOOZED);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
		assertEquals(1, foundCases.get(0).getNotes().size());
		assertAttachmentPresent(foundCases.get(0), FixtureAttachment.COMMENT2);
		assertEquals(6, foundCases.get(1).getNotes().size());
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.TAG_ROUND);
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.CORRELATION01);
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.LINK01, "https://trouble.gov/?ticket=LINK01");
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.LINKEXT1, "https://example.com/articles/LINKEXT1/html");
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.TAG_BLUE);
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.COMMENT1);
		assertEquals(1, foundCases.get(2).getNotes().size());
		assertAttachmentPresent(foundCases.get(2), FixtureAttachment.CORRELATION01);
	}

	@Test
	public void getSnoozedCases_walkThroughCaseList_correctResults() {
		List<FixtureCase> allFixtures = new ArrayList<>(Arrays.asList(
				FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01,
				FixtureCase.SNOOZED04, FixtureCase.SNOOZED03));
		assertCaseOrder(
			"all snoozed cases",
			allFixtures,
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, null, ALL_CASES)
		);
		while (!allFixtures.isEmpty()) {
			FixtureCase firstReceipt = allFixtures.remove(0);
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				wrapCaseFetch(CaseSnoozeFilter.SNOOZED, firstReceipt, ALL_CASES));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void getSnoozedCases_invalidPageActiveCase_exception() {
		wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.ACTIVE01);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getSnoozedCases_invalidPageDeSnoozedCase_exception() {
		wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.DESNOOZED01);
	}

	@Test
	public void getActiveCases_invalidPageInvalidReceipt_firstPageFirstPageReturned() {
		List<? extends CaseSummary> activeCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.ACTIVE),
			PAGE_SIZE, Optional.empty(), Optional.of("NOSUCHANIMAL"), Collections.emptyList());
		assertCaseOrder(activeCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
	}

	@Test
	public void getSnoozedCases_invalidPageInvalidReceipt_firstPageReturned() {
		List<? extends CaseSummary> foundCases = _service.getCases(SYSTEM, CASE_TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED),
			PAGE_SIZE, Optional.empty(), Optional.of("NOSUCHANIMAL"), Collections.emptyList());
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
	}

	@Test
	public void getSnoozedCases_fetchSecondPage_correctResult() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED01);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_firstPageEmptyDateRange_noCases() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.dateRange(RANGE_BEFORE_TIME));
		assertCaseOrder(foundCases);
	}

	@Test
	public void getSnoozedCases_firstPageEarlyDateRange_twoCasesFound() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.dateRange(RANGE_EARLY));
		assertCaseOrder(foundCases, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
	}

	@Test
	public void getSnoozedCases_secondPageEarlyDateRange_noCasesFound() {
		List<? extends CaseSummary> foundCases =
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED01, FilterFactory.dateRange(RANGE_EARLY));
		assertCaseOrder(foundCases);
	}

	@Test
	public void getSnoozedCases_firstPageMiddleDateRange_correctCasesFound() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.dateRange(RANGE_MIDDLE));
		assertCaseOrder(foundCases, FixtureCase.SNOOZED01, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_laterPageMiddleDateRange_caseFound() {
		List<? extends CaseSummary> foundCases =  wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED04, FilterFactory.dateRange(RANGE_MIDDLE));
		assertCaseOrder(foundCases, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_firstPageDefaultReason_correctCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.snoozeReason(DEFAULT_SNOOZE_REASON)),
			FixtureCase.SNOOZED05, FixtureCase.SNOOZED01, FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_firstPageAlternateReason_correctCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.snoozeReason(ALTERNATE_SNOOZE_REASON)),
			FixtureCase.SNOOZED02, FixtureCase.SNOOZED03
		);
	}

	@Test
	public void getSnoozedCases_firstPageBogusReason_noCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.snoozeReason("NOPE"))
		);
	}

	@Test
	public void getSnoozedCases_laterPageDefaultReason_correctCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED01, FilterFactory.snoozeReason(DEFAULT_SNOOZE_REASON)),
			FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_laterPageAlternateReason_correctCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED02, FilterFactory.snoozeReason(ALTERNATE_SNOOZE_REASON)),
			FixtureCase.SNOOZED03
		);
	}

	@Test
	public void getSnoozedCases_laterPageBogusReason_noCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED02, FilterFactory.snoozeReason("NOPE"))
		);
	}

	@Test
	public void getSnoozedCases_firstPageDefaultReasonMiddleRange_correctCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FilterFactory.dateRange(RANGE_MIDDLE), FilterFactory.snoozeReason(DEFAULT_SNOOZE_REASON)),
			FixtureCase.SNOOZED01, FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_laterPageDefaultReasonMiddleRange_correctCasesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.SNOOZED, FixtureCase.SNOOZED01, FilterFactory.dateRange(RANGE_MIDDLE), FilterFactory.snoozeReason(DEFAULT_SNOOZE_REASON)),
			FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getPreviouslySnoozedCases_fetchFirstPage_correctResult() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ALARMED);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03);
	}

	@Test
	public void getPreviouslySnoozedCases_fetchFirstPage_correctAttachments() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ALARMED);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03);
		assertEquals(1, foundCases.get(0).getNotes().size());
		assertAttachmentPresent(foundCases.get(0), FixtureAttachment.CORRELATION01);
		assertEquals(3, foundCases.get(1).getNotes().size());
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.CORRELATION01);
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.LINK01, "https://trouble.gov/?ticket=LINK01");
		assertAttachmentPresent(foundCases.get(1), FixtureAttachment.TAG_BLUE);
		assertEquals(0, foundCases.get(2).getNotes().size());
	}

	@Test
	public void getPreviouslySnoozedCases_fetchSecondPage_correctResult() {
		List<? extends CaseSummary> foundCases = wrapCaseFetch(CaseSnoozeFilter.ALARMED, FixtureCase.DESNOOZED03);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED04);
	}

	@Test
	public void getPreviouslySnoozedCases_walkThroughCaseList_correctResults() {
			List<FixtureCase> allFixtures = new ArrayList<>(Arrays.asList(
			FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03, FixtureCase.DESNOOZED04
		));
		assertCaseOrder(
			"all snoozed cases",
			allFixtures,
			wrapCaseFetch(CaseSnoozeFilter.ALARMED, null, ALL_CASES)
		);
		while (!allFixtures.isEmpty()) {
			FixtureCase firstReceipt = allFixtures.remove(0);
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				wrapCaseFetch(CaseSnoozeFilter.ALARMED, firstReceipt, ALL_CASES));
		}
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageEmptyDateRange_noCases() {
		assertCaseOrder(wrapCaseFetch(CaseSnoozeFilter.ALARMED, FilterFactory.dateRange(RANGE_BEFORE_TIME)));
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageEarlyDateRange_oneCase() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.ALARMED, FilterFactory.dateRange(RANGE_EARLY)),
			FixtureCase.DESNOOZED02
		);
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageMiddleDateRange_casesFound() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.ALARMED, FilterFactory.dateRange(RANGE_MIDDLE)),
			FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03
		);
	}

	@Test
	public void getPreviouslySnoozedCases_secondPageMiddleDateRange_noCases() {
		assertCaseOrder(
			wrapCaseFetch(CaseSnoozeFilter.ALARMED, FixtureCase.DESNOOZED03, FilterFactory.dateRange(RANGE_EARLY))
		);
	}

	private static void assertCaseOrder(String message, List<FixtureCase> expected, List<? extends CaseSummary> foundCases) {
		List<String> foundReceipts = foundCases.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList());
		List<String> expectedReceipts = expected.stream().map(FixtureCase::name).collect(Collectors.toList());
		assertEquals(message, expectedReceipts, foundReceipts);
	}

	private static void assertCaseOrder(List<? extends CaseSummary> foundCases, FixtureCase... expected) {
		List<String> foundReceipts = foundCases.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList());
		List<String> expectedReceipts = Stream.of(expected).map(FixtureCase::name).collect(Collectors.toList());
		assertEquals(expectedReceipts, foundReceipts);
	}

	private static void assertAttachmentPresent(CaseSummary summary, FixtureAttachment fixture) {
		assertAttachmentPresent(summary, fixture, null);
	}

	private static void assertAttachmentPresent(CaseSummary summary, FixtureAttachment fixture, String href) {
		String messageStem = String.format("attachment %s on case %s", fixture, summary.getReceiptNumber());
		for (AttachmentSummary n : summary.getNotes()) {
			LOG.debug("Testing attachment {}, type {}, subtype {}", n.getContent(), n.getType(), n.getSubType());
			if (n.getType() == fixture.getType() && fixture.name().equals(n.getContent())) {
				assertEquals("subtype of " + messageStem, fixture.getSubtype(), n.getSubType());
				if (href != null) {
					assertEquals("href of " + messageStem, href, n.getHref());
				} else {
					assertNull("no href for " + messageStem, n.getHref());
				}
				return;
			}
			LOG.debug("No match with {}", fixture);
		}
		fail(messageStem + " was not found");
	}
}
