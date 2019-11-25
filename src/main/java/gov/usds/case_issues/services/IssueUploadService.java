package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import gov.usds.case_issues.db.model.CaseIssueUpload;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;

@Service
// THIS SERVICE IS NOT TRANSACTIONAL (it launches multiple transactions)
public class IssueUploadService {

	private static final Logger LOG = LoggerFactory.getLogger(IssueUploadService.class);
	@Autowired
	private UploadStatusService _statusService;
	@Autowired
	private CaseListService _listService;

	@SuppressWarnings("checkstyle:IllegalCatch")
	@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).UPDATE_ISSUES.name())")
	public CaseIssueUpload putIssueList(CaseGroupInfo pathInfo, String issueTypeTag, List<CaseRequest> newIssueCases,
			ZonedDateTime eventDate) {
		CaseIssueUpload uploadStatus = _statusService.commenceUpload(
				pathInfo.getCaseManagementSystem(),
				pathInfo.getCaseType(),
				issueTypeTag,
				eventDate,
				newIssueCases.size());
		try {
			LOG.info("Processing upload of {} cases for {}/{}/{}",
					newIssueCases.size(),
					uploadStatus.getCaseManagementSystem().getExternalId(),
					uploadStatus.getCaseType().getExternalId(),
					uploadStatus.getIssueType());
			uploadStatus = _listService.putIssueList(uploadStatus, newIssueCases);
		} catch (Exception e) {
			LOG.error("Issue upload {} failed!", uploadStatus.getInternalId(), e);
			uploadStatus = _statusService.failUpload(uploadStatus);
		}
		return uploadStatus;
	}
}
