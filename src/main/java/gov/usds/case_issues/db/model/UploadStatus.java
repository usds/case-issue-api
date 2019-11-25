package gov.usds.case_issues.db.model;

/**
 * The state of an upload that was requested.
 */
public enum UploadStatus {
	/** The upload request has been received but not completed. */
	STARTED, 
	/** The upload was completed without errors. */
	SUCCESSFUL,
	/** An exception occured during upload processing */
	FAILED,
	/** The upload was canceled (most likely because an uncaught error occurred during processing) */
	CANCELED;
}
