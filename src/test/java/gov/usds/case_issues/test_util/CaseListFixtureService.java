package gov.usds.case_issues.test_util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.services.CaseAttachmentService;

@Service
@Profile("autotest")
public class CaseListFixtureService {

	/** Number of seconds old the fixtures are allowed to get before being recreated */
	private static final int FIXTURE_STALENESS_SECONDS = 30;

	private static final Logger LOG = LoggerFactory.getLogger(CaseListFixtureService.class);

	@Autowired
	private FixtureDataInitializationService _dataService;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;
	@Autowired
	private CaseAttachmentService _attachmentService;
	@Autowired
	private DbTruncator _truncator;

	public static final ZonedDateTime START_DATE = ZonedDateTime.of(2000, 5, 21, 12, 0, 0, 0, ZoneId.of("GMT"));
	public static final String ALTERNATE_SNOOZE_REASON = "NOCOFFEE";
	public static final String DEFAULT_SNOOZE_REASON = "TIREDNOW";
	public static final String ISSUE_TYPE = "PAGING";
	public static final String CASE_TYPE = "McFAKEFAKE";
	public static final String SYSTEM = "FAKEY";

	public static class Keywords {
		public static final String PARITY = "parity";
		private static final String EVEN = "even";
		public static final String ODD = "odd";
	}

	@SuppressWarnings("checkstyle:MagicNumber")
	public enum FixtureCase {
		/** An active case */
		ACTIVE01(CaseListFixtureService.START_DATE,
				Keywords.PARITY, Keywords.ODD),
		/** A case that was opened and closed in the past */
		CLOSED01(CaseListFixtureService.START_DATE.plusHours(24), CaseListFixtureService.START_DATE.plusHours(36),
				Keywords.PARITY, Keywords.EVEN),
		/** A case that is currently open and snoozed */
		SNOOZED01(CaseListFixtureService.START_DATE.plusDays(1), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 10, false,
				Keywords.PARITY, Keywords.ODD),
		/** A case that is currently open and was previously snoozed but is now active */
		DESNOOZED01(CaseListFixtureService.START_DATE.plusDays(2), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 10, true,
				Keywords.PARITY, Keywords.EVEN),
		/** A case that was opened, snoozed, and closed without the snooze ending */
		CLOSED02(CaseListFixtureService.START_DATE.plusDays(2).plusSeconds(1), CaseListFixtureService.START_DATE.plusDays(3), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 100, false,
				Keywords.PARITY, Keywords.ODD),
		// The following two case are reversed to create a conflict between alphabetical and insert-order sorting
		/** See {@link #ACTIVE02} */
		ACTIVE03(CaseListFixtureService.START_DATE.plusDays(3),
				Keywords.PARITY, Keywords.EVEN), // intentional creation date collision
		/** An active case that is functionally identical to another active case ({@link #ACTIVE03}) */
		ACTIVE02(CaseListFixtureService.START_DATE.plusDays(3),
				Keywords.PARITY, Keywords.ODD), // going to explore a paging issue with these
		/** A snoozed case that was created later but snoozed for a shorter time than {@link #SNOOZED01} */
		SNOOZED02(CaseListFixtureService.START_DATE, CaseListFixtureService.ALTERNATE_SNOOZE_REASON, 5, false,
				Keywords.PARITY, Keywords.EVEN),
		/** A desnoozed case that was created earlier but entered into the system later than {@link #DESNOOZED01} */
		DESNOOZED02(CaseListFixtureService.START_DATE.plusDays(1), CaseListFixtureService.ALTERNATE_SNOOZE_REASON, 10, true,
				Keywords.PARITY, Keywords.ODD),
		/** A snoozed case that is snoozed for a reasonably long time. */
		SNOOZED03(CaseListFixtureService.START_DATE.plusDays(3).plusSeconds(1), CaseListFixtureService.ALTERNATE_SNOOZE_REASON, 20, false,
				Keywords.PARITY, Keywords.EVEN),
		/** A snoozed case with a somewhat recent creation date and a moderate snooze length */
		SNOOZED04(CaseListFixtureService.START_DATE.plusDays(5), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 15, false,
				Keywords.PARITY, Keywords.ODD),
		/** A snoozed case with a much more recent creation date and a short snooze length */
		SNOOZED05(CaseListFixtureService.START_DATE.plusDays(180), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 1, false,
				Keywords.PARITY, Keywords.EVEN),
		DESNOOZED03(CaseListFixtureService.START_DATE.plusDays(3).plusSeconds(2), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 5, true,
				Keywords.PARITY, Keywords.ODD),
		DESNOOZED04(CaseListFixtureService.START_DATE.plusDays(10), CaseListFixtureService.DEFAULT_SNOOZE_REASON, 5, true,
				Keywords.PARITY, Keywords.EVEN),
		ACTIVE04(CaseListFixtureService.START_DATE.plusDays(2).plusSeconds(2),
				Keywords.PARITY, Keywords.ODD),
		ACTIVE05(CaseListFixtureService.START_DATE.plusDays(6),
				Keywords.PARITY, Keywords.EVEN),
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

	public enum FixtureAttachment {
		CORRELATION01(AttachmentType.CORRELATION_ID, FixtureCase.SNOOZED01, FixtureCase.SNOOZED02, FixtureCase.DESNOOZED01, FixtureCase.DESNOOZED02),
		LINK01(AttachmentType.LINK, "trouble", FixtureCase.CLOSED02, FixtureCase.SNOOZED02, FixtureCase.DESNOOZED01),
		LINK02(AttachmentType.LINK, "trouble", FixtureCase.SNOOZED03, FixtureCase.SNOOZED04),
		LINKEXT1(AttachmentType.LINK, "external", FixtureCase.SNOOZED02),
		COMMENT1(AttachmentType.COMMENT, FixtureCase.SNOOZED02),
		COMMENT2(AttachmentType.COMMENT, FixtureCase.SNOOZED05, FixtureCase.SNOOZED04),
		TAG_GREEN(AttachmentType.TAG, "color", FixtureCase.DESNOOZED04),
		TAG_BLUE(AttachmentType.TAG, "color", FixtureCase.SNOOZED02, FixtureCase.DESNOOZED01),
		TAG_ROUND(AttachmentType.TAG, "shape", FixtureCase.SNOOZED02),
		;

		private AttachmentType _type;
		private String _subtype;
		private FixtureCase[] _cases;

		private FixtureAttachment(AttachmentType t, FixtureCase... cases) {
			this(t, null, cases);
		}
		private FixtureAttachment(AttachmentType t, String subtype, FixtureCase... cases) {
			_type = t;
			_subtype = subtype;
			_cases = cases;
		}

		public AttachmentType getType() {
			return _type;
		}
		public String getSubtype() {
			return _subtype;
		}

		public AttachmentRequest asRequest() {
			return new AttachmentRequest(_type, name(), _subtype);
		}

		public List<FixtureCase> getCases() {
			return Arrays.asList(_cases);
		}

	}
	
	@Transactional(readOnly=false)
	public void initFixtures() {
		// wipe and re-initialize if the data was created more than 30 seconds ago
		if (_dataService.checkForCaseManagementSystem(SYSTEM, Instant.now().minusSeconds(FIXTURE_STALENESS_SECONDS))) {
			return;
		}
		LOG.info("Clearing DB, and initializing system and type");
		_truncator.truncateAll();
		LOG.info("Initializing case management system and attachment subtypes");
		final CaseManagementSystem sys = _dataService.ensureCaseManagementSystemInitialized(CaseListFixtureService.SYSTEM, "Fake Case Management System for paging/filtering test");
		_dataService.ensureAttachmentSubtype("trouble", AttachmentType.LINK, "https://trouble.gov/?ticket=");
		_dataService.ensureAttachmentSubtype("external", AttachmentType.LINK, "https://example.com/articles/%s/html");
		_dataService.ensureAttachmentSubtype("color", AttachmentType.TAG, null);
		_dataService.ensureAttachmentSubtype("shape", AttachmentType.TAG, null);
		LOG.info("Initializing cases, issues and snoozes");
		Stream.of(FixtureCase.values()).forEach(fixtureConsumer(sys));
		LOG.info("Adding case attachments");
		Stream.of(FixtureAttachment.values()).forEach(attachmentAssociator(sys));
	}

	private Consumer<FixtureCase> fixtureConsumer(CaseManagementSystem sys) {
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

	private Consumer<FixtureAttachment> attachmentAssociator(final CaseManagementSystem sys) {
		return fixture -> {
			AttachmentRequest req = fixture.asRequest();
			for (FixtureCase c : fixture.getCases()) {
				LOG.info("Looking up latest snooze for case {}", c.name());
				TroubleCase mainCase = _caseRepo.findByCaseManagementSystemAndReceiptNumber(sys, c.name()).get();
				CaseSnooze snooze = _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(mainCase).get();
				_attachmentService.attachNote(req, snooze);
			}
		};
	}

}
