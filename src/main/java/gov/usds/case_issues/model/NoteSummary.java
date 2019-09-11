package gov.usds.case_issues.model;

import java.util.Date;

import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.AttachmentType;

public class NoteSummary {

	private String content;
	private AttachmentType type;
	private String subType;
	private String href;
	private String userId;
	private Date timestamp;

	public NoteSummary(CaseAttachmentAssociation backEnd) {
		CaseAttachment note = backEnd.getAttachment();
		type = note.getNoteType();
		content = note.getContent();
		if (null != note.getNoteSubtype()) {
			subType = note.getNoteSubtype().getExternalId();
		}
		if (note.getNoteType() == AttachmentType.LINK) {
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
	public AttachmentType getType() {
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
