package gov.usds.case_issues.db.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

/**
 * Metadata about the case issue data stored in this system
 */
@Entity
public class UserInformation extends UpdatableEntity {

	@NotNull
	private Date lastSeen;
	@NotNull
	private String printName;
	@NaturalId
	@NotNull
	private String userId;

	protected UserInformation() {
		/* for hibernate/JPA */ }

	public UserInformation(String id, String printName) {
		super();
		updateLastSeen();
		this.printName = printName;
		this.userId = id;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public String getPrintName() {
		return printName;
	}

	public String getId() {
		return userId;
	}

	public void updateLastSeen() {
		this.lastSeen = new Date();
	}
}
