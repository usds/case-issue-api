package gov.usds.case_issues.services;


import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UploadStatus;
import gov.usds.case_issues.db.repositories.CaseIssueUploadRepository;

@Service
@Transactional
public class UploadStatusService {

	@Autowired
	private CaseIssueUploadRepository _uploadRepository;

	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public CaseIssueUpload commenceUpload(CaseManagementSystem sys, CaseType caseType, String issueType,
	        ZonedDateTime effectiveDate, int uploadedRecords) {
		return _uploadRepository.save(
		    new CaseIssueUpload(sys, caseType, issueType, effectiveDate, uploadedRecords));
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public CaseIssueUpload completeUpload(CaseIssueUpload upload, long newIssues, long closedIssues) {
		upload.setUploadStatus(UploadStatus.SUCCESSFUL);
		upload.setClosedIssueCount(closedIssues);
		upload.setNewIssueCount(newIssues);
		return _uploadRepository.save(upload);
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public CaseIssueUpload failUpload(CaseIssueUpload upload) {
		upload.setUploadStatus(UploadStatus.FAILED);
		return _uploadRepository.save(upload);
	}

	public CaseIssueUpload readUploadInformation(Long id) {
		return _uploadRepository.findById(id).orElseThrow(
			() -> new IllegalArgumentException("Upload information not found"));
	}
}
