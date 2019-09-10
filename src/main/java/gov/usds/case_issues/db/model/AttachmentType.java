package gov.usds.case_issues.db.model;

/**
 * A type of note/attachment that can be associated with a case/snooze.
 */
public enum AttachmentType {
	/** A simple text comment. */
	COMMENT,
	/**
	 * A value that can be turned into a link (e.g. a trouble ticket in some external system). Requires an associated
	 * NoteSubtype record that will tell us what the rest of the URL for the link is.
	 */
	LINK,
	/**
	 * An internal note that lets the system know about an association between multiple cases/snoozes (generally not to be displayed).
	 */
	CORRELATION_ID,
	/**
	 * A tag that may be applied to multiple cases. Requires an associated NoteSubtype to explain the meaning of the tag.
	 */
	TAG;
}
