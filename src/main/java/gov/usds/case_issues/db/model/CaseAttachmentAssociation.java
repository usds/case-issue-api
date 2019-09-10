package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;

@Entity
public class CaseAttachmentAssociation extends UpdatableEntity {

	@NaturalId
	@ManyToOne(optional=false)
	@JoinColumn(nullable=false)
	private CaseSnooze snooze;
	@NaturalId
	@ManyToOne(optional=false)
	@JoinColumn(nullable=false)
	private CaseAttachment attachment;

	private CaseAttachmentAssociation() { /* for hibernate */}

	public CaseAttachmentAssociation(CaseSnooze snooze, CaseAttachment note) {
		this();
		this.snooze = snooze;
		this.attachment = note;
	}

	public CaseSnooze getSnooze() {
		return snooze;
	}

	public CaseAttachment getAttachment() {
		return attachment;
	}
}
