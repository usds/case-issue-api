package gov.usds.case_issues.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface PersistedCase extends CaseRequest {

	/** The default time zone for external date formatting */
	public static final ZoneId GMT = ZoneId.of("Z");

	/** The date that this case was first uploaded (may or may not be the effective date
	 * of the first upload containing it). */
	ZonedDateTime getCaseInitialUploadDate();

	/** The date that this case was most recently updated (may or may not be the effective
	 * date of the upload that changed it. */
	ZonedDateTime getCaseDataModifiedDate();
}
