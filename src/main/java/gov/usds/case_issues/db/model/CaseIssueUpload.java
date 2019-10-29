package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.DynamicUpdate;

/**
 * A record of an issue upload request (successful or unsuccessful).
 */
@Entity
@DynamicUpdate
public class CaseIssueUpload extends UpdatableEntity {

	@ManyToOne(optional=false)
	@JoinColumn(nullable=false, updatable=false)
	private CaseManagementSystem caseManagementSystem;
	@ManyToOne(optional=false)
	@JoinColumn(nullable=false, updatable=false)
	private CaseType caseType;
	@NotNull
	@Column(nullable=false, updatable=false)
	private String issueType;
	@NotNull
	@Column(nullable=false)
	private ZonedDateTime effectiveDate;
	@NotNull
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private UploadStatus uploadStatus;
	@NotNull
	@Column(nullable=false)
	private long uploadedRecordCount;
	private Long newIssueCount;
	private Long closedIssueCount;

	private CaseIssueUpload() { 
		/* for hibernate/JPA */
		super();
	}

	public CaseIssueUpload(CaseManagementSystem caseManagementSystem, CaseType caseType, @NotNull String issueType,
			ZonedDateTime effectiveDate, @NotNull long uploadedRecordCount) {
		this();
		this.caseManagementSystem = caseManagementSystem;
		this.caseType = caseType;
		this.issueType = issueType;
		this.uploadedRecordCount = uploadedRecordCount;
		if (effectiveDate == null) {
			effectiveDate = ZonedDateTime.now();
		}
		this.effectiveDate = effectiveDate;
		uploadStatus = UploadStatus.STARTED;
	}

	public CaseManagementSystem getCaseManagementSystem() {
		return caseManagementSystem;
	}
	public CaseType getCaseType() {
		return caseType;
	}
	public String getIssueType() {
		return issueType;
	}
	public ZonedDateTime getEffectiveDate() {
		return effectiveDate;
	}
	public UploadStatus getUploadStatus() {
		return uploadStatus;
	}
	public long getUploadedRecordCount() {
		return uploadedRecordCount;
	}
	public Long getNewIssueCount() {
		return newIssueCount;
	}
	public Long getClosedIssueCount() {
		return closedIssueCount;
	}
	public void setUploadStatus(UploadStatus uploadStatus) {
		this.uploadStatus = uploadStatus;
	}
	public void setClosedIssueCount(long closedIssueCount) {
		this.closedIssueCount = closedIssueCount;
	}
	public void setNewIssueCount(long newIssueCount) {
		this.newIssueCount = newIssueCount;
	}

}
