package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;

/**
 * A base class containing only the scalar invariant fields of a trouble case.
 */
@MappedSuperclass
public abstract class TroubleCaseFixedData extends UpdatableEntity {

	@NaturalId
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, nullable = false)
	private CaseManagementSystem caseManagementSystem;
	@NaturalId
	@NotNull
	@Pattern(regexp = "[-\\w]+")
	@Column(updatable = false, nullable = false)
	private String receiptNumber;
	@ManyToOne(optional = false)
	@NotNull
	@JoinColumn(updatable = false, nullable = false)
	private CaseType caseType;
	@NotNull
	@Column(updatable = false, nullable = false)
	private ZonedDateTime caseCreation;

	protected TroubleCaseFixedData() { /* needed for hibernate/JPA */ }

	protected TroubleCaseFixedData(
			@NotNull CaseManagementSystem caseManagementSystem,
			@NotNull @Pattern(regexp = "[-\\w]+") String receiptNumber,
			@NotNull CaseType caseType,
			@NotNull ZonedDateTime caseCreation) {
		this();
		this.caseManagementSystem = caseManagementSystem;
		this.receiptNumber = receiptNumber;
		this.caseType = caseType;
		this.caseCreation = caseCreation;
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
