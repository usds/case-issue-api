package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;

import gov.usds.case_issues.model.ApiViews;

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

	public CaseIssue(TroubleCase issueCase, @NotNull String issueType, @NotNull ZonedDateTime issueCreated) {
		super();
		this.issueCase = issueCase;
		this.issueType = issueType;
		this.issueCreated = issueCreated;
	}

	public Long getCaseIssueId() {
		return caseIssueId;
	}

	public TroubleCase getIssueCase() {
		return issueCase;
	}

	@JsonView(ApiViews.Summary.class)
	public String getIssueType() {
		return issueType;
	}

	@JsonView(ApiViews.Summary.class)
	public ZonedDateTime getIssueCreated() {
		return issueCreated;
	}

	@JsonInclude // this will be null for most interesting cases, but in those cases we can exclude it explicitly
	public ZonedDateTime getIssueClosed() {
		return issueClosed;
	}

}
