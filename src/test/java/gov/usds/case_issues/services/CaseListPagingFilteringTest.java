package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
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
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class CaseListPagingFilteringTest extends CaseIssueApiTestBase {

	private static final String SYSTEM = "FAKEY";
	private static final String CASE_TYPE = "McFAKEFAKE";
	private static final String ISSUE_TYPE = "PAGING";
	private static final String DEFAULT_SNOOZE_REASON = "TIREDNOW";
	private static final ZonedDateTime START_DATE = ZonedDateTime.of(2000, 5, 21, 12, 0, 0, 0, ZoneId.of("GMT"));

	private static final int PAGE_SIZE = 3;

	private static final Logger LOG = LoggerFactory.getLogger(CaseListPagingFilteringTest.class);

	@Autowired
	private CaseListService _service;

	private CaseManagementSystem _sys;
	private CaseType _typ;

	@SuppressWarnings("checkstyle:MagicNumber")
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
		CLOSED02(START_DATE.plusDays(2), START_DATE.plusDays(3), DEFAULT_SNOOZE_REASON, 100, false),
		/** An active case that is functionally identical to another active case ({@link #ACTIVE03}) */
		ACTIVE02(START_DATE.plusDays(3)), // going to explore a paging issue with these
		/** See {@link #ACTIVE02} */
		ACTIVE03(START_DATE.plusDays(3)),
		/** A snoozed case that was created later but snoozed for a shorter time than {@link #SNOOZED01} */
		SNOOZED02(START_DATE, DEFAULT_SNOOZE_REASON, 5, false),
		/** A desnoozed case that was created earlier but entered into the system later than {@link #DESNOOZED01} */
		DESNOOZED02(START_DATE.plusDays(1), DEFAULT_SNOOZE_REASON, 10, true),
		/** A snoozed case that is snoozed for a reasonably long time. */
		SNOOZED03(START_DATE.plusDays(3), DEFAULT_SNOOZE_REASON, 20, false),
		/** A snoozed case with a somewhat recent creation date and a moderate snooze length */
		SNOOZED04(START_DATE.plusDays(5), DEFAULT_SNOOZE_REASON, 15, false),
		/** A snoozed case with a much more recent creation date and a short snooze length */
		SNOOZED05(START_DATE.plusDays(180), DEFAULT_SNOOZE_REASON, 1, false),
		DESNOOZED03(START_DATE.plusDays(3), DEFAULT_SNOOZE_REASON, 5, true),
		DESNOOZED04(START_DATE.plusDays(10), DEFAULT_SNOOZE_REASON, 5, true),
		ACTIVE04(START_DATE.plusDays(2)),
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

	/** This test validates and documents a behavior of active-case sorting that we probably do not want to maintain
	 * in the long run */
	@Test
	public void getActiveCases_interStitialPage_weirdBehaviorIsStable() {
		List<CaseSummary> foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.ACTIVE02.name(), PAGE_SIZE);
		assertEquals(PAGE_SIZE, foundCases.size());
		assertCaseOrder(foundCases, FixtureCase.ACTIVE03, FixtureCase.DESNOOZED03, FixtureCase.ACTIVE05);
		foundCases = _service.getActiveCases(SYSTEM, CASE_TYPE, FixtureCase.ACTIVE03.name(), PAGE_SIZE);
		assertEquals(PAGE_SIZE, foundCases.size());
		assertCaseOrder(foundCases, FixtureCase.ACTIVE02, FixtureCase.DESNOOZED03, FixtureCase.ACTIVE05);
	}

	@Test
	public void getSnoozedCases_fetchFirstPage_correctResult() {
		List<CaseSummary> foundCases = _service.getSnoozedCases(SYSTEM, CASE_TYPE, null, PAGE_SIZE);
		assertCaseOrder(foundCases, FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01);

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

	private static void assertCaseOrder(List<CaseSummary> foundCases, FixtureCase... expected) {
		List<String> foundReceipts = foundCases.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList());
		List<String> expectedReceipts = Stream.of(expected).map(FixtureCase::name).collect(Collectors.toList());
		assertEquals(expectedReceipts, foundReceipts);
	}
}
