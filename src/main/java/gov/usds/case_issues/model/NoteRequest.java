package gov.usds.case_issues.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import gov.usds.case_issues.db.model.NoteType;
import io.swagger.annotations.ApiModelProperty;

/**
 * Thin wrapper to request the creation of a Note through the API.
 */
public class NoteRequest {

	private NoteType noteTypeCode;
	private String noteContent;
	private String subType;

	@JsonProperty(value="type", defaultValue="COMMENT")
	public NoteType getNoteType() {
		return noteTypeCode;
	}

	@JsonProperty("content")
	public String getContent() {
		return noteContent;
	}

	@ApiModelProperty(required=false,
			value="The type code for a NoteSubType entity, which contains information about what kind of tag or link this is.")
	public String getSubType() {
		return subType;
	}
}
