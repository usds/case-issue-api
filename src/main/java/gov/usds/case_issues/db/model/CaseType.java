package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;

/**
 * A type of case adjudicated in a specific {@link CaseManagementSystem}
 * (generally speaking, a single form, but non-form-based cases may exist in the future).
 */
@Entity
public class CaseType {

	@Id
	@GeneratedValue
	private Long caseTypeId;
	@NaturalId
	@ManyToOne
	private CaseManagementSystem caseManagementSystem;
	@NaturalId
	private String caseTypeTag;

	protected CaseType() { /* for hibernate/JPA */ }

	public Long getCaseTypeId() {
		return caseTypeId;
	}

	public CaseManagementSystem getCaseManagementSystem() {
		return caseManagementSystem;
	}

	public String getCaseTypeTag() {
		return caseTypeTag;
	}
}
