package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CaseNote extends WriteOnceEntity {

	@Column(nullable=false)
	private NoteType noteType;
	@ManyToOne(optional=true)
	private NoteSubtype noteSubtype;
	@Column(nullable=false)
	private String content;

	private CaseNote() { /* for hibernate */ }

	public CaseNote(NoteType noteType, NoteSubtype noteSubType, String content) {
		this();
		this.noteType = noteType;
		this.noteSubtype = noteSubType;
		this.content = content;
	}

	public NoteType getNoteType() {
		return noteType;
	}

	public NoteSubtype getNoteSubtype() {
		return noteSubtype;
	}

	public String getContent() {
		return content;
	}
}
