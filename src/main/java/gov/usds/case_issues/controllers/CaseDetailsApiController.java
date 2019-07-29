package gov.usds.case_issues.controllers;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	@Autowired
	private CaseDetailsService _caseDetailsService;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;

	@RequestMapping(method=RequestMethod.GET)
	public CaseDetails getCaseDetails(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		return _caseDetailsService.findCaseDetails(caseManagementSystemTag, receiptNumber);
	}

	@RequestMapping(value="activeSnooze", method = RequestMethod.GET) 
	public ResponseEntity<CaseSnoozeSummary> getActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		Optional<CaseSnooze> snooze = _caseDetailsService.findSnooze(caseManagementSystemTag, receiptNumber);
		if (snoozeIsActive(snooze)) {
			return ResponseEntity.ok(new CaseSnoozeSummaryFacade(snooze));
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	@RequestMapping(value = "activeSnooze", method = RequestMethod.DELETE)
	public ResponseEntity<Void> endActiveSnooze(@PathVariable String caseManagementSystemTag, @PathVariable String receiptNumber) {
		Optional<CaseSnooze> snooze = _caseDetailsService.findSnooze(caseManagementSystemTag, receiptNumber);
		// e-tag could be added here with the end-time of the snooze
		if (snoozeIsActive(snooze)) {
			CaseSnooze caseSnooze = snooze.get();
			caseSnooze.endSnoozeNow();
			_snoozeRepo.save(caseSnooze);
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	private boolean snoozeIsActive(Optional<CaseSnooze> snooze) {
		return snooze.isPresent() && snooze.get().getSnoozeEnd().isAfter(ZonedDateTime.now());
	}

}
