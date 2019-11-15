package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

/**
 * Metadata about the case issue data stored in this system
 */
@Entity
public class UserInformation extends UpdatableEntity {

	@NotNull
	private ZonedDateTime lastSeen;
	@NotNull
	private String printName;
	@NaturalId
	@NotNull
	private String userId;

	protected UserInformation() {
		/* for hibernate/JPA */ }

	public UserInformation(String id, String printName) {
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
