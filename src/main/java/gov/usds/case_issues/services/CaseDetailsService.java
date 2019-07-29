package gov.usds.case_issues.services;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.CaseDetails;

@Service
@Transactional(readOnly=true)
public class CaseDetailsService {

	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private TroubleCaseRepository _caseRepo;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;
	@Autowired
	private CaseIssueRepository _issueRepo;

	public TroubleCase findCaseByTags(String caseManagementSystemTag, String receiptNumber) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		TroubleCase mainCase = _caseRepo.findByCaseManagementSystemAndReceiptNumber(caseManagementSystem, receiptNumber)
				.orElseThrow(()->new ApiModelNotFoundException("Case", receiptNumber));
		return mainCase;
	}

	public Optional<CaseSnooze> findSnooze(String caseManagementSystemTag, String receiptNumber) {
		TroubleCase mainCase = findCaseByTags(caseManagementSystemTag, receiptNumber);
		return _snoozeRepo.findFirstBySnoozeCaseOrderBySnoozeEndDesc(mainCase);
	}

	/**
	 * Find all details about a case, using projection APIs that avoid circular references.
	 * @param caseManagementSystemTag
	 * @param receiptNumber
	 * @return
	 */
	public CaseDetails findCaseDetails(String caseManagementSystemTag, String receiptNumber) {
		TroubleCase mainCase = findCaseByTags(caseManagementSystemTag, receiptNumber);
		Collection<CaseIssueSummary> issues = _issueRepo.findAllByIssueCaseOrderByIssueCreated(mainCase);
		Collection<CaseSnoozeSummary> snoozes = _snoozeRepo.findAllBySnoozeCaseOrderBySnoozeStartAsc(mainCase);
		return new CaseDetails(mainCase, issues, snoozes);
	}
}
