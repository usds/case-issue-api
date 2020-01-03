package gov.usds.case_issues.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.authorization.RequireReadCasePermission;
import gov.usds.case_issues.authorization.RequireUpdateCasePermission;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseDetails;
import gov.usds.case_issues.model.CaseSnoozeSummaryFacade;
import gov.usds.case_issues.model.SnoozeRequest;
import gov.usds.case_issues.services.CaseAttachmentService;
import gov.usds.case_issues.services.CaseDetailsService;

@RestController
@RequestMapping("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}")
@RequireReadCasePermission
public class CaseDetailsApiController {

	@Autowired
	private CaseDetailsService _caseDetailsService;
	@Autowired
	private CaseAttachmentService _attachmentService;

	@GetMapping
	public CaseDetails getCaseDetails(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		return _caseDetailsService.findCaseDetails(caseManagementSystemTag, receiptNumber);
	}

	@GetMapping("activeSnooze")
	public ResponseEntity<CaseSnoozeSummary> getActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		Optional<CaseSnoozeSummary> snooze = _caseDetailsService.findActiveSnooze(caseManagementSystemTag, receiptNumber);
		if (snooze.isPresent()) {
			return ResponseEntity.of(snooze);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	@DeleteMapping("activeSnooze")
	@RequireUpdateCasePermission
	public ResponseEntity<Void> endActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		// e-tag could be added here with the end-time of the snooze
		if (_caseDetailsService.endActiveSnooze(caseManagementSystemTag, receiptNumber)) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	@PutMapping("activeSnooze")
	@RequireUpdateCasePermission
	public ResponseEntity<CaseSnoozeSummaryFacade> changeActiveSnooze(
			@PathVariable String caseManagementSystemTag,
			@PathVariable String receiptNumber,
			@RequestBody @Valid SnoozeRequest requestedSnooze) {
				CaseSnoozeSummaryFacade replacement = _caseDetailsService.updateSnooze(caseManagementSystemTag, receiptNumber, requestedSnooze);
		return ResponseEntity.ok(replacement);
	}

	@GetMapping("attachments")
	public ResponseEntity<List<AttachmentSummary>> getAttachments(@PathVariable String caseManagementSystemTag,
			@PathVariable String receiptNumber) {
		TroubleCase rootCase = _caseDetailsService.findCaseByTags(caseManagementSystemTag, receiptNumber);
		List<AttachmentSummary> attachments = toSummaries(_attachmentService.findAttachmentsForCase(rootCase));
		return ResponseEntity.ok(attachments);
	}

	@PostMapping({"activeSnooze/notes","activeSnooze/attachments"})
	@RequireUpdateCasePermission
	public ResponseEntity<List<AttachmentSummary>> addAttachment(@PathVariable String caseManagementSystemTag,
			@PathVariable String receiptNumber, @RequestBody AttachmentRequest attachment) {
		List<CaseAttachmentAssociation> allAttachments = _caseDetailsService.annotateActiveSnooze(caseManagementSystemTag, receiptNumber, attachment);
		return ResponseEntity.accepted().body(toSummaries(allAttachments));
	}

	private static List<AttachmentSummary> toSummaries(List<CaseAttachmentAssociation> associations) {
		return associations.stream()
			.map(AttachmentSummary::new)
			.collect(Collectors.toList());
	}
}
