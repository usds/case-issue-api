package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Entity
public class AttachmentSubtype extends TaggedEntity {

	@NotNull
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private AttachmentType forAttachmentType;
	private String urlTemplate;

	@SuppressWarnings("unused")
	private AttachmentSubtype() { /* for hibernate */}

	public AttachmentSubtype(String noteSubtypeTag, AttachmentType forNoteType, String name, String description,
			String urlTemplate) {
		super(noteSubtypeTag, name, description);
		this.forAttachmentType = forNoteType;
		this.urlTemplate = urlTemplate;
	}

	public AttachmentType getForAttachmentType() {
		return forAttachmentType;
	}
	public String getUrlTemplate() {
		return urlTemplate;
	}
}
