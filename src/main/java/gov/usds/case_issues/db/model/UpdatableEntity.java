package gov.usds.case_issues.db.model;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * A normal, editable entity, which can be updated one or more times after being created.
 */
@MappedSuperclass
public abstract class UpdatableEntity extends WriteOnceEntity {

	@LastModifiedBy
	private String updatedBy;
	@LastModifiedDate
	private Date updatedAt;

	public String getUpdatedBy() {
		return updatedBy;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}
}
