package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

/**
 * Metadata about the case issue data stored in this system
 */
@Entity
public class User extends UpdatableEntity {

	@NotNull
	private ZonedDateTime lastActive;
	private String name;
	private String userId;

	protected User() {
		/* for hibernate/JPA */ }

	public User(String name, String id) {
		super();
		this.lastActive = ZonedDateTime.now();
		this.name = name;
		this.userId = id;
	}

	public ZonedDateTime getlastActive() {
		return lastActive;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return userId;
	}

	public void setlastActive() {
		this.lastActive = ZonedDateTime.now();
	}
}
