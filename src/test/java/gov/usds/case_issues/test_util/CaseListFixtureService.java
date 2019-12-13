package gov.usds.case_issues.test_util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

@Service
@Profile("autotest")
public class CaseListFixtureService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseListFixtureService.class);

	@Autowired
	private FixtureDataInitializationService _dataService;
	@Autowired
	private DbTruncator _truncator;

	public static final ZonedDateTime START_DATE = ZonedDateTime.of(2000, 5, 21, 12, 0, 0, 0, ZoneId.of("GMT"));
	public static final String ALTERNATE_SNOOZE_REASON = "NOCOFFEE";
	public static final String DEFAULT_SNOOZE_REASON = "TIREDNOW";
	public static final String ISSUE_TYPE = "PAGING";
	public static final String CASE_TYPE = "McFAKEFAKE";
	public static final String SYSTEM = "FAKEY";

	public enum FixtureCase {
		/** An active case */
		ACTIVE01(CaseListFixtureService.START_DATE),
		/** A case that was opened and closed in the past */
		CLOSED01(CaseListFixtureService.START_DATE.plusHours(24), CaseListFixtureService.START_DATE.plusHours(36)),
		/** A case that is currently open and snoozed */
		SNOOZED01(CaseListFixtureService.START_DATE.plusDays(1), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 10, false),
		/** A case that is currently open and was previously snoozed but is now active */
		DESNOOZED01(CaseListFixtureService.START_DATE.plusDays(2), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 10, true),
		/** A case that was opened, snoozed, and closed without the snooze ending */
		CLOSED02(CaseListFixtureService.START_DATE.plusDays(2).plusSeconds(1), CaseListFixtureService.START_DATE.plusDays(3), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 100, false),
		// The following two case are reversed to create a conflict between alphabetical and insert-order sorting
		/** See {@link #ACTIVE02} */
		ACTIVE03(CaseListFixtureService.START_DATE.plusDays(3)), // intentional creation date collision
		/** An active case that is functionally identical to another active case ({@link #ACTIVE03}) */
		ACTIVE02(CaseListFixtureService.START_DATE.plusDays(3)), // going to explore a paging issue with these
		/** A snoozed case that was created later but snoozed for a shorter time than {@link #SNOOZED01} */
		SNOOZED02(CaseListFixtureService.START_DATE, CaseListFixtureService.ALTERNATE_SNOOZE_REASON, 5, false),
		/** A desnoozed case that was created earlier but entered into the system later than {@link #DESNOOZED01} */
		DESNOOZED02(CaseListFixtureService.START_DATE.plusDays(1), CaseListFixtureService.ALTERNATE_SNOOZE_REASON, 10, true),
		/** A snoozed case that is snoozed for a reasonably long time. */
		SNOOZED03(CaseListFixtureService.START_DATE.plusDays(3).plusSeconds(1), CaseListFixtureService.ALTERNATE_SNOOZE_REASON, 20, false),
		/** A snoozed case with a somewhat recent creation date and a moderate snooze length */
		SNOOZED04(CaseListFixtureService.START_DATE.plusDays(5), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 15, false),
		/** A snoozed case with a much more recent creation date and a short snooze length */
		SNOOZED05(CaseListFixtureService.START_DATE.plusDays(180), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 1, false),
		DESNOOZED03(CaseListFixtureService.START_DATE.plusDays(3).plusSeconds(2), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 5, true),
		DESNOOZED04(CaseListFixtureService.START_DATE.plusDays(10), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 5, true),
		ACTIVE04(CaseListFixtureService.START_DATE.plusDays(2).plusSeconds(2)),
		ACTIVE05(CaseListFixtureService.START_DATE.plusDays(6)),
		;
	
		public final ZonedDateTime startDate;
		public final ZonedDateTime endDate;
		public final String[] keyValues;
		public final String snoozeReason;
		public final int snoozeDays;
		public final boolean terminateSnooze;
	
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

	@Transactional(readOnly=false)
	public void initFixtures() {
		if (_dataService.checkForCaseManagementSystem(SYSTEM)) {
			return;
		}
		LOG.info("Clearing DB, and initializing system and type");
		_truncator.truncateAll();
		LOG.info("Initializing cases, issues and snoozes");
		Stream.of(CaseListFixtureService.FixtureCase.values()).forEach(fixtureConsumer());
	}

	private Consumer<FixtureCase> fixtureConsumer() {
		final CaseManagementSystem sys = _dataService.ensureCaseManagementSystemInitialized(CaseListFixtureService.SYSTEM, "Fake Case Management System for paging/filtering test");
		final CaseType typ = _dataService.ensureCaseTypeInitialized(CaseListFixtureService.CASE_TYPE, "Fake Case Type for paging/filtering test");
		return template -> {
			LOG.info("Initializing {} as a {} case", template, template.snoozeDays >  0 ? "snoozed" : "unsnoozed");
			TroubleCase tc = _dataService.initCaseAndIssue(sys,
				template.name(),
				typ,
				template.startDate,
				CaseListFixtureService.ISSUE_TYPE,
				template.endDate,
				template.keyValues
			);
			if (template.snoozeReason != null) {
				_dataService.snoozeCase(tc, template.snoozeReason, template.snoozeDays, template.terminateSnooze);
			}
		};
	}

}
