package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.test_util.CaseListFixtureService;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureAttachment;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureCase;

import static gov.usds.case_issues.test_util.CaseListFixtureService.SYSTEM;
import static gov.usds.case_issues.test_util.CaseListFixtureService.CASE_TYPE;
import static gov.usds.case_issues.test_util.CaseListFixtureService.START_DATE;
import static gov.usds.case_issues.test_util.CaseListFixtureService.DEFAULT_SNOOZE_REASON;
import static gov.usds.case_issues.test_util.CaseListFixtureService.ALTERNATE_SNOOZE_REASON;

@SuppressWarnings("checkstyle:MagicNumber")
public abstract class CaseListPagingFilteringTest extends CaseIssueApiTestBase {

	/** The date range from 1 to 6 days after our start time */
	private static final DateRange RANGE_MIDDLE = new DateRange(START_DATE.plusDays(1), START_DATE.plusDays(6));
	/** The date range from prehistory to exactly 1 day after our start time. */
	private static final DateRange RANGE_EARLY = new DateRange(START_DATE.minusYears(1), START_DATE.plusDays(1));
	/** The date range from "a long time ago" to the day before our earliest case. */
	private static final DateRange RANGE_BEFORE_TIME = new DateRange(START_DATE.minusYears(1), START_DATE.minusDays(1));

	private static final int PAGE_SIZE = 3;

	private static final Logger LOG = LoggerFactory.getLogger(CaseListPagingFilteringTest.class);

	@Autowired
	private CaseListFixtureService _fixtureService;

	@Before
	public void initPageableData() {
		_fixtureService.initFixtures();
	}

	protected abstract CasePagingService getService();

	@Test
	public void getActiveCases_firstPage_correctResult() {
		List<? extends CaseSummary> activeCases = getService().getActiveCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertEquals(PAGE_SIZE, activeCases.size());
		assertCaseOrder(activeCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
	}
	@Test
	public void getActiveCases_firstPage_correctAttachments() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
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
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED01.name(), PAGE_SIZE);
		assertEquals(PAGE_SIZE, foundCases.size());
		assertCaseOrder(foundCases, FixtureCase.ACTIVE04, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03);
	}

	// exhaustively test paged requests for stability
	@Test
	public void getActiveCases_walkThroughCaseList_correctResults() {
		int includeAllCases = FixtureCase.values().length;
		List<FixtureCase> allFixtures = new ArrayList<>(Arrays.asList(
			FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01,
			FixtureCase.ACTIVE04, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03,
			FixtureCase.DESNOOZED03, FixtureCase.ACTIVE05, FixtureCase.DESNOOZED04
		));
		assertCaseOrder(
			"all cases",
			allFixtures,
			getService().getActiveCases(SYSTEM, CASE_TYPE, null, includeAllCases)
		);
		while (!allFixtures.isEmpty()) {
			String firstReceipt = allFixtures.remove(0).name();
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				getService().getActiveCases(SYSTEM, CASE_TYPE, firstReceipt, includeAllCases));
		}
	}

	@Test
	public void getActiveCases_firstPageEmptyDateRange_noCases() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, null,
			RANGE_BEFORE_TIME,
			PAGE_SIZE);
		assertEquals(0, foundCases.size());
	}

	@Test
	public void getActiveCases_firstPageEarlyDateRange_twoCases() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, null,
			RANGE_EARLY, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02);
	}

	@Test
	public void getActiveCases_nextPageEarlyDateRange_noCases() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED02.name(),
			RANGE_EARLY, PAGE_SIZE);
		assertCaseOrder(foundCases);
	}

	@Test
	public void getActiveCases_firstPageMiddleDateRange_correctCases() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, null,
			RANGE_MIDDLE, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.ACTIVE04);
	}



	@Test
	public void getActiveCases_secondPageMiddleDateRange_correctCases() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.ACTIVE04.name(),
			RANGE_MIDDLE, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03, FixtureCase.DESNOOZED03);
	}

	@Test
	public void getActiveCases_lastPageMiddleDateRange_correctCases() {
		List<? extends CaseSummary> foundCases = getService().getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED03.name(),
			RANGE_MIDDLE, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE05);
	}

	@Test
	public void getSnoozedCases_fetchFirstPage_correctResult() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
	}

	@Test
	public void getSnoozedCases_fetchFirstPage_correctAttachments() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
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

	// exhaustively test paged requests for stability
	@Test
	public void getSnoozedCases_walkThroughCaseList_correctResults() {
		int includeAllCases = FixtureCase.values().length;
		List<FixtureCase> allFixtures = new ArrayList<>(Arrays.asList(
				FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01,
				FixtureCase.SNOOZED04, FixtureCase.SNOOZED03));
		assertCaseOrder(
			"all snoozed cases",
			allFixtures,
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, null, includeAllCases)
		);
		while (!allFixtures.isEmpty()) {
			String firstReceipt = allFixtures.remove(0).name();
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				getService().getSnoozedCases(SYSTEM, CASE_TYPE, firstReceipt, includeAllCases));
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void getSnoozedCases_invalidPageActiveCase_exception() {
		getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.ACTIVE01.name(), PAGE_SIZE);
	}

	@Test(expected=IllegalArgumentException.class)
	public void getSnoozedCases_invalidPageDeSnoozedCase_exception() {
		getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED01.name(), PAGE_SIZE);
	}

	@Test
	public void getActiveCases_invalidPageInvalidReceipt_firstPageFirstPageReturned() {
		List<? extends CaseSummary> activeCases = getService().getActiveCases(SYSTEM, CASE_TYPE, "NOSUCHANIMAL", PAGE_SIZE);
		assertCaseOrder(activeCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
	}

	@Test
	public void getSnoozedCases_invalidPageInvalidReceipt_firstPageReturned() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, "NOSUCHANIMAL", PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);

	}

	@Test
	public void getSnoozedCases_fetchSecondPage_correctResult() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_firstPageEmptyDateRange_noCases() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_BEFORE_TIME, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases);
	}

	@Test
	public void getSnoozedCases_firstPageEarlyDateRange_twoCasesFound() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_EARLY, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
	}

	@Test
	public void getSnoozedCases_secondPageEarlyDateRange_noCasesFound() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(), RANGE_EARLY, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases);
	}

	@Test
	public void getSnoozedCases_firstPageMiddleDateRange_correctCasesFound() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_MIDDLE, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED01, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_laterPageMiddleDateRange_caseFound() {
		List<? extends CaseSummary> foundCases = getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED04.name(), RANGE_MIDDLE, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_firstPageDefaultReason_correctCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, null,
					null, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED05, FixtureCase.SNOOZED01, FixtureCase.SNOOZED04
		);
	}
	@Test
	public void getSnoozedCases_firstPageAlternateReason_correctCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, null,
					null, Optional.of(ALTERNATE_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED02, FixtureCase.SNOOZED03
		);
	}

	@Test
	public void getSnoozedCases_firstPageBogusReason_noCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, null,
					null, Optional.of("NOPE"), PAGE_SIZE)
		);
	}

	@Test
	public void getSnoozedCases_laterPageDefaultReason_correctCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(),
					null, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_laterPageAlternateReason_correctCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED02.name(),
					null, Optional.of(ALTERNATE_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED03
		);
	}

	@Test
	public void getSnoozedCases_laterPageBogusReason_noCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED02.name(),
					null, Optional.of("NOPE"), PAGE_SIZE)
		);
	}

	@Test
	public void getSnoozedCases_firstPageDefaultReasonMiddleRange_correctCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, null,
					RANGE_MIDDLE, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED01, FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_laterPageDefaultReasonMiddleRange_correctCasesFound() {
		assertCaseOrder(
			getService().getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(),
					RANGE_MIDDLE, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getPreviouslySnoozedCases_fetchFirstPage_correctResult() {
		List<? extends CaseSummary> foundCases = getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03);
	}
	@Test
	public void getPreviouslySnoozedCases_fetchFirstPage_correctAttachments() {
		List<? extends CaseSummary> foundCases = getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
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
		List<? extends CaseSummary> foundCases = getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED03.name(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED04);
	}

	// exhaustively test paged requests for stability
	@Test
	public void getPreviouslySnoozedCases_walkThroughCaseList_correctResults() {
		int includeAllCases = FixtureCase.values().length;
		List<FixtureCase> allFixtures = new ArrayList<>(Arrays.asList(
			FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03, FixtureCase.DESNOOZED04
		));
		assertCaseOrder(
			"all snoozed cases",
			allFixtures,
			getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, includeAllCases)
		);
		while (!allFixtures.isEmpty()) {
			String firstReceipt = allFixtures.remove(0).name();
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, firstReceipt, includeAllCases));
		}
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageEmptyDateRange_noCases() {
		assertCaseOrder(getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_BEFORE_TIME, PAGE_SIZE));
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageEarlyDateRange_oneCase() {
		assertCaseOrder(
			getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_EARLY, PAGE_SIZE),
			FixtureCase.DESNOOZED02
		);
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageMiddleDateRange_casesFound() {
		assertCaseOrder(
			getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_MIDDLE, PAGE_SIZE),
			FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03
		);
	}

	@Test
	public void getPreviouslySnoozedCases_secondPageMiddleDateRange_noCases() {
		assertCaseOrder(
			getService().getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED03.name(), RANGE_MIDDLE, PAGE_SIZE)
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
