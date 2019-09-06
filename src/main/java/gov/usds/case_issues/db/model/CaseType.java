package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A type of case adjudicated in a specific {@link CaseManagementSystem}
 * (generally speaking, a single form, but non-form-based cases may exist in the future).
 */
@Entity
public class CaseType extends UpdatableEntity {

	@NaturalId
	@NotNull
	@Pattern(regexp="[-\\w]+")
	@JsonProperty("tag")
	private String caseTypeTag;
	private String name;
	private String description;

	protected CaseType() { /* for hibernate/JPA */ }

	public CaseType(String tag, String name, String desc) {
		this.caseTypeTag = tag;
		this.name = name;
		this.description = desc;
	}

	public String getCaseTypeTag() {
		return caseTypeTag;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
