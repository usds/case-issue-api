package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;

@Entity
public class NoteAssociation extends UpdatableEntity {

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

	public CaseSnooze getSnooze() {
		return snooze;
	}

	public CaseNote getNote() {
		return note;
	}
}
