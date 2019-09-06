package gov.usds.case_issues.db.model;

import java.util.Date;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An entity that is auditable, but never updated once written. (e.g. a note, which
 * can be unlinked from a case but not deleted).
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class WriteOnceEntity {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long internalId;

	public Long getInternalId() {
		return internalId;
	}

	@CreatedBy
	private String createdBy;

	@CreatedDate
	private Date createdAt;

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

}
