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
	private ZonedDateTime lastSeen;
	private String printName;
	private String userId;

	protected User() {
		/* for hibernate/JPA */ }

	public User(String printName, String id) {
		super();
		this.lastSeen = ZonedDateTime.now();
		this.printName = printName;
		this.userId = id;
	}

	public ZonedDateTime getLastSeen() {
		return lastSeen;
	}

	public String getPrintName() {
		return printName;
	}

	public String getId() {
		return userId;
	}

	public void updateLastSeen() {
		this.lastSeen = ZonedDateTime.now();
	}
}
