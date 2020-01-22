package gov.usds.case_issues.services;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.BatchUpdateRequestErrors;
import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.BatchUpdateAction;
import gov.usds.case_issues.model.BatchUpdateRequest;
import gov.usds.case_issues.model.BatchUpdateRequestException;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@SuppressWarnings("checkstyle:MagicNumber")
public class BatchUpdateServiceTest extends CaseIssueApiTestBase {

	private static final String ISSUE_TYPE = "BATCHISSUE";
	private static final String WRONG_TYPE = "OTHERONE";
	private static final String TYPE = "NORMAL-ISH";
	private static final String SYSTEM = "BATCHFRIENDLY";

	@Autowired
	private BatchUpdateService _service;
	@Autowired
	private CaseFilteringService _filterService;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void reset() {
		truncateDb();
		CaseManagementSystem sys = _dataService.ensureCaseManagementSystemInitialized(SYSTEM, "Batch test system");
		CaseType typ = _dataService.ensureCaseTypeInitialized(TYPE, "Cases we will update");
		_dataService.ensureCaseTypeInitialized(WRONG_TYPE, "Cases we will not update");
		_dataService.initCaseAndOpenIssue(sys, "QWERTY1", typ, ZonedDateTime.now(), ISSUE_TYPE);
		_dataService.initCaseAndOpenIssue(sys, "QWERTY2", typ, ZonedDateTime.now(), ISSUE_TYPE);
		TroubleCase c = _dataService.initCaseAndOpenIssue(sys, "PRESNOOZED1", typ, ZonedDateTime.now(), ISSUE_TYPE);
		_dataService.snoozeCase(c);
		c = _dataService.initCaseAndOpenIssue(sys, "PRESNOOZED2", typ, ZonedDateTime.now(), ISSUE_TYPE);
		_dataService.snoozeCase(c);
	}

	@Test
	public void processBatchAction_badCaseGroup_notFound() {
		expected.expect(ApiModelNotFoundException.class);
		_service.processBatchAction(SYSTEM, "NOPE", null);
	}

	@Test
	public void processBatchAction_attachNoAttachments_expectedError() {
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.ATTACH, Arrays.asList("ABC","DEF"),Collections.emptyList(), Optional.empty(), 0);
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("No attachments provided for bulk attachment request");
		_service.processBatchAction(SYSTEM, TYPE, request);
	}

	@Test
	public void processBatchAction_snoozeMissingArguments_expectedError() {
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.BEGIN_SNOOZE, Arrays.asList("ABC","DEF"),Collections.emptyList(), Optional.empty(), 0);
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage(Matchers.containsString("reason and positive duration"));
		_service.processBatchAction(SYSTEM, TYPE, request);
	}

	@Test
	public void processBatchAction_invalidCases_expectedError() {
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.BEGIN_SNOOZE, Arrays.asList("ABC","DEF"), Collections.emptyList(),
				Optional.of("BECAUSE"), 1);
		expected.expect(BatchUpdateRequestException.class);
		expected.expect(hasProperty("errors",
			allOf(
				isA(BatchUpdateRequestErrors.class),
				hasProperty("missingReceipts", equalTo(Arrays.asList("ABC","DEF")))
			)
		));
		_service.processBatchAction(SYSTEM, TYPE, request);
	}

	@Test
	public void processBatchAction_wrongTypeCases_expectedError() {
		List<String> receipts = Arrays.asList("QWERTY1","QWERTY2");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.BEGIN_SNOOZE, receipts, Collections.emptyList(),
				Optional.of("BECAUSE"), 1);
		expected.expect(BatchUpdateRequestException.class);
		expected.expect(hasProperty("errors",
			allOf(
				isA(BatchUpdateRequestErrors.class),
				hasProperty("invalidCaseTypeReceipts", equalTo(receipts))
			)
		));
		_service.processBatchAction(SYSTEM, WRONG_TYPE, request);
	}

	@Test
	public void processBatchAction_endSnoozeActiveCases_expectedError() {
		List<String> receipts = Arrays.asList("QWERTY1","QWERTY2");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.END_SNOOZE, receipts, Collections.emptyList(),
				Optional.of("BECAUSE"), 1);
		expected.expect(BatchUpdateRequestException.class);
		expected.expect(hasProperty("errors",
			allOf(
				isA(BatchUpdateRequestErrors.class),
				hasProperty("ineligibleForActionReceipts", equalTo(receipts))
			)
		));
		_service.processBatchAction(SYSTEM, TYPE, request);
	}

	@Test
	public void processBatchAction_attachToActiveCases_expectedError() {
		List<String> receipts = Arrays.asList("QWERTY1","QWERTY2");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.ATTACH, receipts,
				Collections.singletonList(new AttachmentRequest(AttachmentType.COMMENT, "meh")),
				Optional.of("BECAUSE"), 1);
		expected.expect(BatchUpdateRequestException.class);
		expected.expect(hasProperty("errors",
			allOf(
				isA(BatchUpdateRequestErrors.class),
				hasProperty("ineligibleForActionReceipts", equalTo(receipts))
			)
		));
		_service.processBatchAction(SYSTEM, TYPE, request);
	}

	@Test
	public void processBatchAction_snoozeAlreadySnoozedCases_expectedError() {
		List<String> receipts = Arrays.asList("PRESNOOZED1");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.BEGIN_SNOOZE, receipts,
				Collections.singletonList(new AttachmentRequest(AttachmentType.COMMENT, "meh")),
				Optional.of("BECAUSE"), 1);
		expected.expect(BatchUpdateRequestException.class);
		expected.expect(hasProperty("errors",
			allOf(
				isA(BatchUpdateRequestErrors.class),
				hasProperty("ineligibleForActionReceipts", equalTo(receipts))
			)
		));
		_service.processBatchAction(SYSTEM, TYPE, request);
	}

	@Test
	public void processBatchAction_beginSnoozeWithNotes_snoozedWithIds() {
		List<String> receipts = Arrays.asList("QWERTY1", "QWERTY2");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.BEGIN_SNOOZE, receipts,
				Collections.singletonList(new AttachmentRequest(AttachmentType.COMMENT, "meh")),
				Optional.of("BECAUSE"), 1);
		List<CaseAttachment> result = _service.processBatchAction(SYSTEM, TYPE, request);
		assertEquals(AttachmentType.CORRELATION_ID, result.get(0).getAttachmentType());
		assertEquals(2, result.size());
		List<CaseSummary> found = _filterService.getCases(
				SYSTEM, TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED), 3,
				Optional.empty(),
				Optional.empty(),
				Collections.singletonList(FilterFactory.snoozeReason("BECAUSE")));
		assertEquals(2, found.size());
		assertEquals(receipts, found.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList()));
		assertEquals(
			result.stream().map(CaseAttachment::getContent).collect(Collectors.toSet()),
			found.get(0).getNotes().stream().map(AttachmentSummary::getContent).collect(Collectors.toSet())
		);
		assertEquals(
			result.stream().map(CaseAttachment::getInternalId).collect(Collectors.toSet()),
			found.get(0).getNotes().stream().map(AttachmentSummary::getId).collect(Collectors.toSet())
		);
	}

	@Test
	public void processBatchAction_endSnooze_snoozesEnded() {
		List<String> receipts = Arrays.asList("PRESNOOZED1", "PRESNOOZED2");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.END_SNOOZE, receipts,
				Collections.singletonList(new AttachmentRequest(AttachmentType.COMMENT, "re-check these")),
				Optional.empty(), 0);
		List<CaseAttachment> result = _service.processBatchAction(SYSTEM, TYPE, request);
		assertEquals(AttachmentType.CORRELATION_ID, result.get(0).getAttachmentType());
		assertEquals(2, result.size());
		List<CaseSummary> found = _filterService.getCases(
				SYSTEM, TYPE, Collections.singleton(CaseSnoozeFilter.ALARMED), 3,
				Optional.empty(),
				Optional.empty(),
				Collections.emptyList());
		assertEquals(2, found.size());
		assertEquals(receipts, found.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList()));
		assertEquals(
			result.stream().map(CaseAttachment::getContent).collect(Collectors.toSet()),
			found.get(0).getNotes().stream().map(AttachmentSummary::getContent).collect(Collectors.toSet())
		);
		assertEquals(
			result.stream().map(CaseAttachment::getInternalId).collect(Collectors.toSet()),
			found.get(0).getNotes().stream().map(AttachmentSummary::getId).collect(Collectors.toSet())
		);
	}

	@Test
	public void processBatchAction_attachToSnooze_attachmentsMade() {
		List<String> receipts = Arrays.asList("PRESNOOZED1", "PRESNOOZED2");
		BatchUpdateRequest request = new BatchUpdateRequest(
				BatchUpdateAction.ATTACH, receipts,
				Collections.singletonList(new AttachmentRequest(AttachmentType.COMMENT, "re-check these")),
				Optional.empty(), 0);
		List<CaseAttachment> result = _service.processBatchAction(SYSTEM, TYPE, request);
		assertEquals(AttachmentType.COMMENT, result.get(0).getAttachmentType());
		assertEquals(1, result.size());
		List<CaseSummary> found = _filterService.getCases(
				SYSTEM, TYPE, Collections.singleton(CaseSnoozeFilter.SNOOZED), 3,
				Optional.empty(),
				Optional.empty(),
				Arrays.asList(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.COMMENT, null))));
		assertEquals(2, found.size());
		assertEquals(receipts, found.stream().map(CaseSummary::getReceiptNumber).collect(Collectors.toList()));
		assertEquals(
			result.stream().map(CaseAttachment::getContent).collect(Collectors.toSet()),
			found.get(0).getNotes().stream().map(AttachmentSummary::getContent).collect(Collectors.toSet())
		);
		assertEquals(
			result.stream().map(CaseAttachment::getInternalId).collect(Collectors.toSet()),
			found.get(0).getNotes().stream().map(AttachmentSummary::getId).collect(Collectors.toSet())
		);
	}

}