package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Entity
public class CaseAttachment extends WriteOnceEntity {

	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private AttachmentType attachmentType;
	@ManyToOne(optional=true)
	private AttachmentSubtype attachmentSubtype;
	@Column(nullable=false)
	private String content;

	private CaseAttachment() { /* for hibernate */ }

	public CaseAttachment(AttachmentType noteType, AttachmentSubtype noteSubType, String content) {
		this();
		this.attachmentType = noteType;
		this.attachmentSubtype = noteSubType;
		this.content = content;
	}

	public AttachmentType getNoteType() {
		return attachmentType;
	}

	public AttachmentSubtype getNoteSubtype() {
		return attachmentSubtype;
	}

	public String getContent() {
		return content;
	}
}
