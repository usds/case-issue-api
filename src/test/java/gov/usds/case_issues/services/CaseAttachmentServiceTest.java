package gov.usds.case_issues.services;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class CaseAttachmentServiceTest extends CaseIssueApiTestBase {

	private TroubleCase _rootCase;
	private TroubleCase _otherCase;

	@Autowired
	private CaseAttachmentService _service;

	@Before
	public void init() {
		truncateDb();
		CaseManagementSystem sys = _dataService.ensureCaseManagementSystemInitialized("BACK", "Attachment Test CMS");
		CaseType typ = _dataService.ensureCaseTypeInitialized("ATCHA", "Attachment test case type");
		_rootCase = _dataService.initCaseAndOpenIssue(sys, "FAAAKEY", typ, ZonedDateTime.now(), "BOGUS");
		_otherCase = _dataService.initCaseAndOpenIssue(sys, "BAAAKEY", typ, ZonedDateTime.now(), "BOGUS");
	}

	@Test
	public void findAttachmentsForCase_noAttachments_emptyList() {
		List<CaseAttachmentAssociation> found = _service.findAttachmentsForCase(_rootCase);
		assertNotNull(found);
		assertEquals(0, found.size());
	}

	@Test
	public void findAttachmentsForCase_oneComment_attachmentFound() {
		CaseSnooze snooze = _dataService.snoozeCase(_rootCase);
		String noteContent = "Hello, notes";
		AttachmentRequest request = new AttachmentRequest(AttachmentType.COMMENT, noteContent);
		_service.attachToSnooze(request, snooze);
		List<CaseAttachmentAssociation> found = _service.findAttachmentsForCase(_rootCase);
		assertNotNull(found);
		assertEquals(1, found.size());
		assertEquals(noteContent, found.get(0).getAttachment().getContent());
		assertEquals(AttachmentType.COMMENT, found.get(0).getAttachment().getAttachmentType());	
	}


	@Test
	public void findAttachmentsForCase_oneLink_attachmentFound() {
		String subtypeTag = "LINKYLINK";
		_dataService.ensureAttachmentSubtypeInitialized(subtypeTag, "Valid link type", AttachmentType.LINK, "https://%s.example.com");
		CaseSnooze snooze = _dataService.snoozeCase(_rootCase);
		String noteContent = "article1";
		AttachmentRequest request = new AttachmentRequest(AttachmentType.LINK, noteContent, subtypeTag);
		_service.attachToSnooze(request, snooze);
		List<CaseAttachmentAssociation> found = _service.findAttachmentsForCase(_rootCase);
		assertNotNull(found);
		assertEquals(1, found.size());
		assertEquals(noteContent, found.get(0).getAttachment().getContent());
		assertEquals(AttachmentType.LINK, found.get(0).getAttachment().getAttachmentType());	
	}

	@Test
	public void attachToSnooze_multipleSnoozesSameContent_sameAttachment() {
		CaseSnooze snooze = _dataService.snoozeCase(_rootCase);
		CaseSnooze otherSnooze = _dataService.snoozeCase(_otherCase);
		String noteContent = "There can be only one";
		_service.attachToSnooze(new AttachmentRequest(AttachmentType.COMMENT, noteContent), snooze);
		_service.attachToSnooze(new AttachmentRequest(AttachmentType.COMMENT, noteContent), otherSnooze);
		List<CaseAttachmentAssociation> found = _service.findAttachmentsForCase(_rootCase);
		List<CaseAttachmentAssociation> otherFound = _service.findAttachmentsForCase(_otherCase);
		assertNotEquals(found.get(0).getInternalId(), otherFound.get(0).getInternalId());
		assertEquals(found.get(0).getAttachment().getInternalId(), otherFound.get(0).getAttachment().getInternalId());
	}

	@Test // this is banned by CaseDetailsService, but not here. Consistency issue?
	public void attachToSnooze_activeCase_noError() throws Exception {
		CaseSnooze snooze = _dataService.snoozeCase(_rootCase, "SLEEPY", 1, true);
		_service.attachToSnooze(new AttachmentRequest(AttachmentType.COMMENT, "No comment"), snooze);
		List<CaseAttachmentAssociation> found = _service.findAttachmentsForCase(_rootCase);
		assertNotNull(found);
		assertEquals(1, found.size());
		assertEquals("No comment", found.get(0).getAttachment().getContent());
		assertEquals(AttachmentType.COMMENT, found.get(0).getAttachment().getAttachmentType());	
	}

	@Test(expected=IllegalArgumentException.class)
	public void attachToSnooze_invalidSubtype_error() {
		doFailingAttachmentCall(new AttachmentRequest(AttachmentType.LINK, "12345", "deadlink"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void attachToSnooze_missingSubtype_error() {
		doFailingAttachmentCall(new AttachmentRequest(AttachmentType.LINK, "12345", null));
	}

	@Test(expected=IllegalArgumentException.class)
	public void attachToSnooze_validSubtypeWrongType_error() {
		_dataService.ensureAttachmentSubtypeInitialized("taggy", "Tag type", AttachmentType.TAG, null);
		doFailingAttachmentCall(new AttachmentRequest(AttachmentType.LINK, "12345", "taggy"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void attachToSnooze_uselessSubtype_error() {
		_dataService.ensureAttachmentSubtypeInitialized("taggy", "Tag type", AttachmentType.TAG, null);
		doFailingAttachmentCall(new AttachmentRequest(AttachmentType.COMMENT, "12345", "taggy"));
	}

	private void doFailingAttachmentCall(AttachmentRequest request) {
		CaseSnooze snooze = _dataService.snoozeCase(_rootCase);
		_service.attachToSnooze(request, snooze);
	}
}
