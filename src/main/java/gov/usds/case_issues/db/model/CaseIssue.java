package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

/**
 * An issue associated with a case.
 */
@Entity
public class CaseIssue {

	@Id
	@GeneratedValue
	private Long caseIssueId;
	@NaturalId
	@ManyToOne(optional=false)
	private TroubleCase issueCase;
	@NaturalId
	@NotNull
	private String issueType; // Needs a foreign key relationship
	@NaturalId
	@NotNull
	private ZonedDateTime issueCreated;
	private ZonedDateTime issueClosed;
	
	protected CaseIssue() { /* for hibernate/JPA */ }

	public Long getCaseIssueId() {
		return caseIssueId;
	}

	public TroubleCase getIssueCase() {
		return issueCase;
	}

	public String getIssueType() {
		return issueType;
	}

	public ZonedDateTime getIssueCreated() {
		return issueCreated;
	}

	public ZonedDateTime getIssueClosed() {
		return issueClosed;
	}
}
