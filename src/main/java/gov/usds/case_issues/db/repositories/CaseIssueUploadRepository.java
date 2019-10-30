package gov.usds.case_issues.db.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.UploadStatus;

/**
 * CRUD repository for upload objects.
 */
public interface CaseIssueUploadRepository extends CrudRepository<CaseIssueUpload, Long> {

	public List<CaseIssueUpload> findAllByCaseManagementSystem(
			CaseManagementSystem sys);
	public List<CaseIssueUpload> findAllByCaseManagementSystemAndCaseType(
			CaseManagementSystem sys, CaseType type);
	public List<CaseIssueUpload> findAllByCaseManagementSystemAndCaseTypeAndIssueType(
			CaseManagementSystem sys, CaseType type, String issueType);
	public List<CaseIssueUpload> findAllByCaseManagementSystemAndCaseTypeAndIssueTypeAndUploadStatus(
			CaseManagementSystem sys, CaseType type, String issueType, UploadStatus uploadStatus);
	public Optional<CaseIssueUpload> findFirstByCaseManagementSystemAndCaseTypeAndIssueTypeAndUploadStatusOrderByEffectiveDateDesc(
			CaseManagementSystem sys, CaseType type, String issueType, UploadStatus uploadStatus);
	public Optional<CaseIssueUpload> findFirstByCaseManagementSystemAndCaseTypeAndUploadStatusOrderByEffectiveDateDesc(
			CaseManagementSystem sys, CaseType type, UploadStatus uploadStatus);
}
