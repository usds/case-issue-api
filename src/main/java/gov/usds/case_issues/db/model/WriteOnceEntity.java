package gov.usds.case_issues.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

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

	public static final String DEFAULT_SEQUENCE_GENERATOR = "caseIssueDefaultSequence";

	@Id
	@GeneratedValue(
		strategy=GenerationType.SEQUENCE,
		generator=DEFAULT_SEQUENCE_GENERATOR
	)
	@SequenceGenerator(
		name=DEFAULT_SEQUENCE_GENERATOR,
		sequenceName="case_issue_entity_id_sequence"
	)
	@JsonIgnore
	@Column(updatable=false)
	private Long internalId;

	@CreatedBy
	@Column(updatable=false)
	private String createdBy;

	@CreatedDate
	@Column(updatable=false)
	private Date createdAt;

	@ManyToOne(optional=true)
	@JoinColumn(name="createdBy", referencedColumnName="userId",
		insertable=false, updatable=false)
	@JsonIgnore
	private UserInformation creationUser;

	public Long getInternalId() {
		return internalId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public UserInformation getCreationUser() {
		return creationUser;
	}
}
