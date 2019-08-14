package gov.usds.case_issues.model;

import java.time.ZonedDateTime;

import gov.usds.case_issues.db.model.NoteType;

public class NoteSummary {

	private String content;
	private NoteType type;
	private String subType;
	private String href;
	private ZonedDateTime timestamp;

	public String getContent() {
		return content;
	}
	public NoteType getType() {
		return type;
	}
	public String getSubType() {
		return subType;
	}
	public String getHref() {
		return href;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}
}
