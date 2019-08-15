package gov.usds.case_issues.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usds.case_issues.db.model.NoteType;
import io.swagger.annotations.ApiModelProperty;

/**
 * Thin wrapper to request the creation of a Note through the API.
 */
public class NoteRequest {

	private NoteType noteTypeCode;
	private String content;
	private String subType;

	protected NoteRequest() {
		super();
	}

	public NoteRequest(NoteType typeCode, String content) {
		this();
		this.content = content;
		this.noteTypeCode = typeCode;
	}

	public NoteRequest(NoteType noteTypeCode, String noteContent, String subType) {
		this(noteTypeCode, noteContent);
		this.subType = subType;
	}

	@JsonProperty(value="type", defaultValue="COMMENT")
	public NoteType getNoteType() {
		return noteTypeCode;
	}

	@JsonProperty("content")
	public String getContent() {
		return content;
	}

	@ApiModelProperty(required=false,
			value="The type code for a NoteSubType entity, which contains information about what kind of tag or link this is.")
	@JsonAlias("subType")
	public String getSubtype() {
		return subType;
	}

	public void setNoteType(NoteType noteTypeCode) {
		this.noteTypeCode = noteTypeCode;
	}

	public void setContent(String noteContent) {
		this.content = noteContent;
	}

	public void setSubtype(String subType) {
		this.subType = subType;
	}
}
