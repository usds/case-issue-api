package gov.usds.case_issues.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.model.AttachmentType;

public class AttachmentSummary {

	private long attachmentId;
	private String content;
	private AttachmentType type;
	private String subType;
	private String href;
	private ZonedDateTime timestamp;
	private SerializedUserInformation user;

	public AttachmentSummary(CaseAttachmentAssociation backEnd) {
		CaseAttachment note = backEnd.getAttachment();
		attachmentId = note.getInternalId();
		UserInformation associationCreator = backEnd.getCreationUser();
		type = note.getAttachmentType();
		content = note.getContent();
		if (null != note.getSubtype()) {
			subType = note.getSubtype().getExternalId();
		}
		if (note.getAttachmentType() == AttachmentType.LINK) {
			String urlTemplate = note.getSubtype().getUrlTemplate();
			if (urlTemplate.contains("%s")) {
				href= String.format(urlTemplate, content);
			} else {
				href = urlTemplate + content;
			}
		}
		if (associationCreator != null) {
			user = new SerializedUserInformation(associationCreator);
		} else {
			user = new SerializedUserInformation(backEnd.getCreatedBy(), "");
		}

		timestamp = ZonedDateTime.ofInstant(backEnd.getCreatedAt().toInstant(), ZoneId.of("Z"));
	}

	public long getId() {
		return attachmentId;
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

	public SerializedUserInformation getUser() {
		return user;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}
}
