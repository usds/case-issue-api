package gov.usds.case_issues.controllers;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.model.CaseDetails;
import gov.usds.case_issues.model.CaseSnoozeSummaryFacade;
import gov.usds.case_issues.services.CaseDetailsService;

@RestController
@RequestMapping("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}")
public class CaseDetailsApiController {

	private static final Logger LOG = LoggerFactory.getLogger(CaseDetailsApiController.class);

	@Autowired
	private CaseDetailsService _caseDetailsService;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;

	@GetMapping
	public CaseDetails getCaseDetails(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		return _caseDetailsService.findCaseDetails(caseManagementSystemTag, receiptNumber);
	}

	@GetMapping("activeSnooze")
	public ResponseEntity<CaseSnoozeSummary> getActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		Optional<CaseSnooze> snooze = _caseDetailsService.findSnooze(caseManagementSystemTag, receiptNumber);
		if (snoozeIsActive(snooze)) {
			return ResponseEntity.ok(new CaseSnoozeSummaryFacade(snooze));
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	@DeleteMapping("activeSnooze")
	public ResponseEntity<Void> endActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		Optional<CaseSnooze> snooze = _caseDetailsService.findSnooze(caseManagementSystemTag, receiptNumber);
		// e-tag could be added here with the end-time of the snooze
		LOG.debug("Ending current snooze on {}/{}", caseManagementSystemTag, receiptNumber);
		if (snoozeIsActive(snooze)) {
			CaseSnooze caseSnooze = snooze.get();
			caseSnooze.endSnoozeNow();
			_snoozeRepo.save(caseSnooze);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	@PutMapping("activeSnooze")
	public ResponseEntity<CaseSnoozeSummary> changeActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber,
			 @RequestBody Map<String,?> newSnoozeInformation) {
		Optional<CaseSnooze> snooze = _caseDetailsService.findSnooze(caseManagementSystemTag, receiptNumber);
		if (snoozeIsActive(snooze)) {
			CaseSnooze caseSnooze = snooze.get();
			caseSnooze.endSnoozeNow();
			_snoozeRepo.save(caseSnooze);
		}
		String reason = (String) newSnoozeInformation.get("reason");
		String details = (String) newSnoozeInformation.get("details");
		Integer duration = (Integer) newSnoozeInformation.get("duration");
		LOG.debug("Replacing snooze on {}/{} with {} for {} days",
				caseManagementSystemTag, receiptNumber, reason, duration);
		CaseSnooze replacement = new CaseSnooze(snooze.get().getSnoozeCase(), reason, duration, details);
		_snoozeRepo.save(replacement);
		return ResponseEntity.ok(new CaseSnoozeSummaryFacade(replacement));
	}

	private boolean snoozeIsActive(Optional<CaseSnooze> snooze) {
		return snooze.isPresent() && snooze.get().getSnoozeEnd().isAfter(ZonedDateTime.now());
	}

}
