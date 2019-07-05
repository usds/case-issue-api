package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A type of case adjudicated in a specific {@link CaseManagementSystem}
 * (generally speaking, a single form, but non-form-based cases may exist in the future).
 */
@Entity
public class CaseType {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long caseTypeId;
	@NaturalId
	@ManyToOne(optional=false)
	private CaseManagementSystem caseManagementSystem;
	@NaturalId
	@NotNull
	private String caseTypeTag;
	private String description;

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

	public String getDescription() {
		return description;
	}
}
