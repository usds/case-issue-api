package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Entity
public class NoteSubtype extends TaggedEntity {

	@NotNull
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private NoteType forNoteType;
	private String urlTemplate;

	@SuppressWarnings("unused")
	private NoteSubtype() { /* for hibernate */}

	public NoteSubtype(String noteSubtypeTag, NoteType forNoteType, String name, String description,
			String urlTemplate) {
		super(noteSubtypeTag, name, description);
		this.forNoteType = forNoteType;
		this.urlTemplate = urlTemplate;
	}

	public NoteType getForNoteType() {
		return forNoteType;
	}
	public String getUrlTemplate() {
		return urlTemplate;
	}
}
