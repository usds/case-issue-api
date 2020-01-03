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
	private String id;
	private String name;
	private ZonedDateTime timestamp;

	public AttachmentSummary(CaseAttachmentAssociation backEnd) {
		CaseAttachment note = backEnd.getAttachment();
		attachmentId = note.getInternalId();
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
		id = backEnd.getCreatedBy();
		name = "";
		timestamp = ZonedDateTime.ofInstant(backEnd.getCreatedAt().toInstant(), ZoneId.of("Z"));
	}

	public AttachmentSummary(CaseAttachmentAssociation backEnd, UserInformation user) {
		this(backEnd);
		id = user != null ? user.getId() : backEnd.getCreatedBy();
		name = user != null ? user.getPrintName() : "";
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
		return new SerializedUserInformation(id, name);
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}
}
