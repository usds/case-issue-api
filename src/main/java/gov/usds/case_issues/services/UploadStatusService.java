package gov.usds.case_issues.services;


import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(UploadStatusService.class);

	@Autowired
	private CaseIssueUploadRepository _uploadRepository;

	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public CaseIssueUpload commenceUpload(CaseManagementSystem sys, CaseType caseType, String issueType,
	        ZonedDateTime effectiveDate, int uploadedRecords) {
		LOG.debug("Saving upload record for {}/{}/{}", sys.getExternalId(), caseType.getExternalId(), issueType);
		return _uploadRepository.save(
		    new CaseIssueUpload(sys, caseType, issueType, effectiveDate, uploadedRecords));
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public CaseIssueUpload completeUpload(CaseIssueUpload upload) {
		LOG.debug("Finalizing upload record {} as success", upload.getInternalId());
		upload.setUploadStatus(UploadStatus.SUCCESSFUL);
		return _uploadRepository.save(upload);
	}

	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	public CaseIssueUpload failUpload(CaseIssueUpload upload) {
		LOG.debug("Finalizing upload record {} as failure", upload.getInternalId());
		upload.setUploadStatus(UploadStatus.FAILED);
		return _uploadRepository.save(upload);
	}

	/** Return <b>all</b> uploads (successful and otherwise) for this system and case type,
	 * sorted by effective date (not by created date, unless we change our minds).
	 */
	public List<CaseIssueUpload> getUploadHistory(CaseManagementSystem sys, CaseType type) {
		List<CaseIssueUpload> history = _uploadRepository.findAllByCaseManagementSystemAndCaseType(sys, type);
		history.sort((a,b)->a.getEffectiveDate().compareTo(b.getEffectiveDate()));
		return history;
	}
	public CaseIssueUpload getLastUpload(CaseManagementSystem sys, CaseType type, String issueTypeTag) {
		return _uploadRepository.findFirstByCaseManagementSystemAndCaseTypeAndIssueTypeAndUploadStatusOrderByEffectiveDateDesc(
				sys, type, issueTypeTag, UploadStatus.SUCCESSFUL).orElse(null);
	}
	public CaseIssueUpload getLastUpload(CaseManagementSystem sys, CaseType type, UploadStatus successful) {
		return _uploadRepository.findFirstByCaseManagementSystemAndCaseTypeAndUploadStatusOrderByEffectiveDateDesc(
				sys, type, UploadStatus.SUCCESSFUL).orElse(null);
	}

	/** Simple fetch-by-ID, for something where people rarely want to know the ID: initially just for test/verification */ 
	public CaseIssueUpload readUploadInformation(Long id) {
		return _uploadRepository.findById(id).orElseThrow(
			() -> new IllegalArgumentException("Upload information not found"));
	}
}
