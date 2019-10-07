package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Metadata about the case issue data stored in this system
 */
@Entity
public class CaseMetadata extends UpdatableEntity {

	@NotNull
	private ZonedDateTime lastUpdated;

	protected CaseMetadata() { /* for hibernate/JPA */ }

	public CaseMetadata(ZonedDateTime lastUpdated) {
		super();
		this.lastUpdated = lastUpdated;
	}

	public ZonedDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
