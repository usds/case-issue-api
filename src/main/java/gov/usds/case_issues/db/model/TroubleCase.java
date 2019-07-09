package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A case that has been seen, at some point, by this system
 * (and hence must, at some point, have had an issue).
 */
/* And yes, "Case" would be a simpler name, until you remember that it's a reserved word in every language ever */
@Entity
public class TroubleCase {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long internalCaseId;

	@NaturalId
	@ManyToOne(optional=false)
	private CaseManagementSystem caseManagementSystem;
	@NaturalId
	private String receiptNumber;

	@ManyToOne(optional=false)
	private CaseType caseType;
	@NotNull
	private ZonedDateTime caseCreation;

	protected TroubleCase() {/* for hibernate/JPA */}

	public Long getInternalCaseId() {
		return internalCaseId;
	}

	public CaseManagementSystem getCaseManagementSystem() {
		return caseManagementSystem;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public CaseType getCaseType() {
		return caseType;
	}

	public ZonedDateTime getCaseCreation() {
		return caseCreation;
	}
}
