package gov.usds.case_issues.model;

import java.util.Date;

import gov.usds.case_issues.db.model.CaseNote;
import gov.usds.case_issues.db.model.NoteAssociation;
import gov.usds.case_issues.db.model.NoteType;

public class NoteSummary {

	private String content;
	private NoteType type;
	private String subType;
	private String href;
	private String userId;
	private Date timestamp;

	public NoteSummary(NoteAssociation backEnd) {
		CaseNote note = backEnd.getNote();
		type = note.getNoteType();
		content = note.getContent();
		if (null != note.getNoteSubtype()) {
			subType = note.getNoteSubtype().getNoteSubtypeTag();
		}
		if (note.getNoteType() == NoteType.LINK) {
			String urlTemplate = note.getNoteSubtype().getUrlTemplate();
			if (urlTemplate.contains("%s")) {
				href= String.format(urlTemplate, content);
			} else {
				href = urlTemplate + content;
			}
		}
		userId = backEnd.getCreatedBy();
		timestamp = backEnd.getCreatedAt();
	}

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

	public String getUserId() {
		return userId;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}
