package gov.usds.case_issues.db.model;

import java.util.Date;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A normal, editable entity, which can be updated one or more times after being created.
 */
@MappedSuperclass
public abstract class UpdatableEntity extends WriteOnceEntity {

	@LastModifiedBy
	private String updatedBy;
	@LastModifiedDate
	private Date updatedAt;

	@ManyToOne(optional=true)
	@JoinColumn(name="updatedBy", referencedColumnName="userId",
		insertable=false, updatable=false)
	@JsonIgnore
	private UserInformation updateUser;

	public String getUpdatedBy() {
		return updatedBy;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public UserInformation getUpdateUser() {
		return updateUser;
	}
}
