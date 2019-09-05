package gov.usds.case_issues.db.model;

import javax.persistence.Entity;

@Entity
public class NoteSubtype extends TaggedEntity {

	private NoteType forNoteType;
	private String name;
	private String description;
	private String urlTemplate;

	@SuppressWarnings("unused")
	private NoteSubtype() { /* for hibernate */}

	public NoteSubtype(String noteSubtypeTag, NoteType forNoteType, String name, String description,
			String urlTemplate) {
		super(noteSubtypeTag);
		this.forNoteType = forNoteType;
		this.name = name;
		this.description = description;
		this.urlTemplate = urlTemplate;
	}

	public NoteType getForNoteType() {
		return forNoteType;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getUrlTemplate() {
		return urlTemplate;
	}
}
