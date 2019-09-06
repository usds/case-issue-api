package gov.usds.case_issues.db.model;

import javax.persistence.Entity;

/**
 * A type of case adjudicated in a specific {@link CaseManagementSystem}
 * (generally speaking, a single form, but non-form-based cases may exist in the future).
 */
@Entity
public class CaseType extends TaggedEntity {

	protected CaseType() { /* for hibernate/JPA */ }

	public CaseType(String tag, String name, String desc) {
		super(tag, name, desc);
	}
}
