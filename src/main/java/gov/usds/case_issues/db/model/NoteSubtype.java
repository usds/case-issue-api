package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class NoteSubtype extends UpdatableEntity {


	@NaturalId(mutable=false)
	@Column(nullable=false)
	@JsonProperty("tag")
	private String noteSubtypeTag;
	private NoteType forNoteType;
	private String name;
	private String description;
	private String urlTemplate;

	private NoteSubtype() { /* for hibernate */}

	public NoteSubtype(String noteSubtypeTag, NoteType forNoteType, String name, String description,
			String urlTemplate) {
		this();
		this.noteSubtypeTag = noteSubtypeTag;
		this.forNoteType = forNoteType;
		this.name = name;
		this.description = description;
		this.urlTemplate = urlTemplate;
	}

	public String getNoteSubtypeTag() {
		return noteSubtypeTag;
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
