package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;

@Entity
public class NoteAssociation {

	@Id
	@GeneratedValue
	private Long associationId;
	@CreatedDate       // this one should probably work, but didn't on first try...
	@CreationTimestamp // this works, but gets the time from Java: just make postgresql do it on insert
	private ZonedDateTime associationTimestamp;
	@NaturalId
	@ManyToOne(optional=false)
	private CaseSnooze snooze;
	@NaturalId
	@ManyToOne(optional=false)
	private CaseNote note;

	private NoteAssociation() { /* for hibernate */}

	public NoteAssociation(CaseSnooze snooze, CaseNote note) {
		this();
		this.snooze = snooze;
		this.note = note;
	}

	public Long getAssociationId() {
		return associationId;
	}

	public ZonedDateTime getAssociationTimestamp() {
		return associationTimestamp;
	}

	public CaseSnooze getSnooze() {
		return snooze;
	}

	public CaseNote getNote() {
		return note;
	}
}
