package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@SuppressWarnings("checkstyle:MagicNumber")
public class CaseListPagingFilteringTest extends CaseIssueApiTestBase {

	private static final String SYSTEM = "FAKEY";
	private static final String CASE_TYPE = "McFAKEFAKE";
	private static final String ISSUE_TYPE = "PAGING";
	private static final String DEFAULT_SNOOZE_REASON = "TIREDNOW";
	private static final String ALTERNATE_SNOOZE_REASON = "NOCOFFEE";
	private static final ZonedDateTime START_DATE = ZonedDateTime.of(2000, 5, 21, 12, 0, 0, 0, ZoneId.of("GMT"));

	/** The date range from 1 to 6 days after our start time */
	private static final DateRange RANGE_MIDDLE = new DateRange(START_DATE.plusDays(1), START_DATE.plusDays(6));
	/** The date range from prehistory to exactly 1 day after our start time. */
	private static final DateRange RANGE_EARLY = new DateRange(START_DATE.minusYears(1), START_DATE.plusDays(1));
	/** The date range from "a long time ago" to the day before our earliest case. */
	private static final DateRange RANGE_BEFORE_TIME = new DateRange(START_DATE.minusYears(1), START_DATE.minusDays(1));

	private static final int PAGE_SIZE = 3;

	private static final Logger LOG = LoggerFactory.getLogger(CaseListPagingFilteringTest.class);

	@Autowired
	private CaseListService _service;

	private CaseManagementSystem _sys;
	private CaseType _typ;

	public enum FixtureCase {
		/** An active case */
		ACTIVE01(START_DATE),
		/** A case that was opened and closed in the past */
		CLOSED01(START_DATE.plusHours(24), START_DATE.plusHours(36)),
		/** A case that is currently open and snoozed */
		SNOOZED01(START_DATE.plusDays(1), DEFAULT_SNOOZE_REASON, 10, false),
		/** A case that is currently open and was previously snoozed but is now active */
		DESNOOZED01(START_DATE.plusDays(2), DEFAULT_SNOOZE_REASON, 10, true),
		/** A case that was opened, snoozed, and closed without the snooze ending */
		CLOSED02(START_DATE.plusDays(2).plusSeconds(1), START_DATE.plusDays(3), DEFAULT_SNOOZE_REASON, 100, false),
		// The following two case are reversed to create a conflict between alphabetical and insert-order sorting
		/** See {@link #ACTIVE02} */
		ACTIVE03(START_DATE.plusDays(3)), // intentional creation date collision
		/** An active case that is functionally identical to another active case ({@link #ACTIVE03}) */
		ACTIVE02(START_DATE.plusDays(3)), // going to explore a paging issue with these
		/** A snoozed case that was created later but snoozed for a shorter time than {@link #SNOOZED01} */
		SNOOZED02(START_DATE, ALTERNATE_SNOOZE_REASON, 5, false),
		/** A desnoozed case that was created earlier but entered into the system later than {@link #DESNOOZED01} */
		DESNOOZED02(START_DATE.plusDays(1), ALTERNATE_SNOOZE_REASON, 10, true),
		/** A snoozed case that is snoozed for a reasonably long time. */
		SNOOZED03(START_DATE.plusDays(3).plusSeconds(1), ALTERNATE_SNOOZE_REASON, 20, false),
		/** A snoozed case with a somewhat recent creation date and a moderate snooze length */
		SNOOZED04(START_DATE.plusDays(5), DEFAULT_SNOOZE_REASON, 15, false),
		/** A snoozed case with a much more recent creation date and a short snooze length */
		SNOOZED05(START_DATE.plusDays(180), DEFAULT_SNOOZE_REASON, 1, false),
		DESNOOZED03(START_DATE.plusDays(3).plusSeconds(2), DEFAULT_SNOOZE_REASON, 5, true),
		DESNOOZED04(START_DATE.plusDays(10), DEFAULT_SNOOZE_REASON, 5, true),
		ACTIVE04(START_DATE.plusDays(2).plusSeconds(2)),
		ACTIVE05(START_DATE.plusDays(6)),
		;

		final ZonedDateTime startDate;
		final ZonedDateTime endDate;
		final String[] keyValues;
		final String snoozeReason;
		final int snoozeDays;
		final boolean terminateSnooze;

		private FixtureCase(ZonedDateTime startDate, String... keyValues) {
			this(startDate, null, keyValues);
		}

		private FixtureCase(ZonedDateTime startDate, ZonedDateTime endDate, String... keyValues) {
			this(startDate, endDate, null, 0, false, keyValues);
		}

		private FixtureCase(ZonedDateTime startDate, String snoozeReason, int snoozeDays, boolean cancelSnooze, String... keyValues) {
			this(startDate, null, snoozeReason, snoozeDays, cancelSnooze, keyValues);
		}

		private FixtureCase(ZonedDateTime startDate, ZonedDateTime endDate, String snoozeReason, int snoozeDays, boolean cancelSnooze, String... keyValues) {
			this.startDate = startDate;
			this.keyValues = keyValues;
			this.endDate = endDate;
			this.snoozeReason = snoozeReason;
			this.snoozeDays = snoozeDays;
			this.terminateSnooze = cancelSnooze;
		}
	}

	@Before
	public void initPageableData() {
		if (_dataService.checkForCaseManagementSystem(SYSTEM)) {
			return;
		}
		LOG.info("Clearing DB, and initializing system and type");
		this.truncateDb();
		_sys = _dataService.ensureCaseManagementSystemInitialized(SYSTEM, "Fake Case Management System for paging/filtering test");
		_typ = _dataService.ensureCaseTypeInitialized(CASE_TYPE, "Fake Case Type for paging/filtering test");
		LOG.info("Initializing cases, issues and snoozes");
		Stream.of(FixtureCase.values()).forEach(this::enumeratedCase);
	}

	private TroubleCase enumeratedCase(FixtureCase template) {
		LOG.info("Initializing {} as a {} case", template, template.snoozeDays >  0 ? "snoozed" : "unsnoozed");
		TroubleCase tc = _dataService.initCaseAndIssue(_sys,
			template.name(),
			_typ,
			template.startDate,
			ISSUE_TYPE,
			template.endDate,
			template.keyValues
		);
		if (template.snoozeReason != null) {
			_dataService.snoozeCase(tc, template.snoozeReason, template.snoozeDays, template.terminateSnooze);
		}
		return tc;
	}

	@Test
	public void getActiveCases_firstPage_correctResult() {
		List<CaseSummary> activeCases = _service.getActiveCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertEquals(PAGE_SIZE, activeCases.size());
		assertCaseOrder(activeCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
	}

	@Test
	public void getActiveCases_secondPage_correctResult() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED01.name(), PAGE_SIZE);
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
			_service.getActiveCases(SYSTEM, CASE_TYPE, null, includeAllCases)
		);
		while (!allFixtures.isEmpty()) {
			String firstReceipt = allFixtures.remove(0).name();
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				_service.getActiveCases(SYSTEM, CASE_TYPE, firstReceipt, includeAllCases));
		}
	}

	@Test
	public void getActiveCases_firstPageEmptyDateRange_noCases() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, null,
			RANGE_BEFORE_TIME,
			PAGE_SIZE);
		assertEquals(0, foundCases.size());
	}

	@Test
	public void getActiveCases_firstPageEarlyDateRange_twoCases() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, null,
			RANGE_EARLY, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02);
	}

	@Test
	public void getActiveCases_nextPageEarlyDateRange_noCases() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED02.name(),
			RANGE_EARLY, PAGE_SIZE);
		assertCaseOrder(foundCases);
	}

	@Test
	public void getActiveCases_firstPageMiddleDateRange_correctCases() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, null,
			RANGE_MIDDLE, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.ACTIVE04);
	}

	@Test
	public void getActiveCases_secondPageMiddleDateRange_correctCases() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.ACTIVE04.name(),
			RANGE_MIDDLE, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE02, FixtureCase.ACTIVE03, FixtureCase.DESNOOZED03);
	}

	@Test
	public void getActiveCases_lastPageMiddleDateRange_correctCases() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED03.name(),
			RANGE_MIDDLE, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.ACTIVE05);
	}

	@Test
	public void getSnoozedCases_fetchFirstPage_correctResult() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
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
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, null, includeAllCases)
		);
		while (!allFixtures.isEmpty()) {
			String firstReceipt = allFixtures.remove(0).name();
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				_service.getSnoozedCases(SYSTEM, CASE_TYPE, firstReceipt, includeAllCases));
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void getSnoozedCases_invalidPageActiveCase_exception() {
		_service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.ACTIVE01.name(), PAGE_SIZE);
	}

	@Test(expected=IllegalArgumentException.class)
	public void getSnoozedCases_invalidPageDeSnoozedCase_exception() {
		_service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED01.name(), PAGE_SIZE);
	}

	@Test
	public void getActiveCases_invalidPageInvalidReceipt_firstPageFirstPageReturned() {
		List<CaseSummary> activeCases = _service.getActiveCases(SYSTEM, CASE_TYPE, "NOSUCHANIMAL", PAGE_SIZE);
		assertCaseOrder(activeCases, FixtureCase.ACTIVE01, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01);
	}

	@Test
	public void getSnoozedCases_invalidPageInvalidReceipt_firstPageReturned() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, "NOSUCHANIMAL", PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);

	}

	@Test
	public void getSnoozedCases_fetchSecondPage_correctResult() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_firstPageEmptyDateRange_noCases() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_BEFORE_TIME, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases);
	}

	@Test
	public void getSnoozedCases_firstPageEarlyDateRange_twoCasesFound() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_EARLY, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);
	}

	@Test
	public void getSnoozedCases_secondPageEarlyDateRange_noCasesFound() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(), RANGE_EARLY, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases);
	}

	@Test
	public void getSnoozedCases_firstPageMiddleDateRange_correctCasesFound() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_MIDDLE, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED01, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_laterPageMiddleDateRange_caseFound() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED04.name(), RANGE_MIDDLE, Optional.empty(), PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED03);
	}

	@Test
	public void getSnoozedCases_firstPageDefaultReason_correctCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, null,
					null, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED05, FixtureCase.SNOOZED01, FixtureCase.SNOOZED04
		);
	}
	@Test
	public void getSnoozedCases_firstPageAlternateReason_correctCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, null,
					null, Optional.of(ALTERNATE_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED02, FixtureCase.SNOOZED03
		);
	}

	@Test
	public void getSnoozedCases_firstPageBogusReason_noCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, null,
					null, Optional.of("NOPE"), PAGE_SIZE)
		);
	}

	@Test
	public void getSnoozedCases_laterPageDefaultReason_correctCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(),
					null, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_laterPageAlternateReason_correctCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED02.name(),
					null, Optional.of(ALTERNATE_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED03
		);
	}

	@Test
	public void getSnoozedCases_laterPageBogusReason_noCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED02.name(),
					null, Optional.of("NOPE"), PAGE_SIZE)
		);
	}

	@Test
	public void getSnoozedCases_firstPageDefaultReasonMiddleRange_correctCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, null,
					RANGE_MIDDLE, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED01, FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getSnoozedCases_laterPageDefaultReasonMiddleRange_correctCasesFound() {
		assertCaseOrder(
			_service.getSnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.SNOOZED01.name(),
					RANGE_MIDDLE, Optional.of(DEFAULT_SNOOZE_REASON), PAGE_SIZE),
			FixtureCase.SNOOZED04
		);
	}

	@Test
	public void getPreviouslySnoozedCases_fetchFirstPage_correctResult() {
		List<CaseSummary> foundCases = _service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03);
	}

	@Test
	public void getPreviouslySnoozedCases_fetchSecondPage_correctResult() {
		List<CaseSummary> foundCases = _service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED03.name(), PAGE_SIZE);
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
			_service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, includeAllCases)
		);
		while (!allFixtures.isEmpty()) {
			String firstReceipt = allFixtures.remove(0).name();
			String message = "page after case " + firstReceipt;
			assertCaseOrder(message, allFixtures,
				_service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, firstReceipt, includeAllCases));
		}
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageEmptyDateRange_noCases() {
		assertCaseOrder(_service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_BEFORE_TIME, PAGE_SIZE));
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageEarlyDateRange_oneCase() {
		assertCaseOrder(
			_service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_EARLY, PAGE_SIZE),
			FixtureCase.DESNOOZED02
		);
	}

	@Test
	public void getPreviouslySnoozedCases_firstPageMiddleDateRange_casesFound() {
		assertCaseOrder(
			_service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, null, RANGE_MIDDLE, PAGE_SIZE),
			FixtureCase.DESNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED03
		);
	}

	@Test
	public void getPreviouslySnoozedCases_secondPageMiddleDateRange_noCases() {
		assertCaseOrder(
			_service.getPreviouslySnoozedCases(SYSTEM, CASE_TYPE, FixtureCase.DESNOOZED03.name(), RANGE_MIDDLE, PAGE_SIZE)
		);
	}

	private static void assertCaseOrder(String message, List<FixtureCase> expected, List<CaseSummary> foundCases) {
		List<String> foundReceipts = foundCases.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList());
		List<String> expectedReceipts = expected.stream().map(FixtureCase::name).collect(Collectors.toList());
		assertEquals(message, expectedReceipts, foundReceipts);
	}

	private static void assertCaseOrder(List<CaseSummary> foundCases, FixtureCase... expected) {
		List<String> foundReceipts = foundCases.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList());
		List<String> expectedReceipts = Stream.of(expected).map(FixtureCase::name).collect(Collectors.toList());
		assertEquals(expectedReceipts, foundReceipts);
	}
}
