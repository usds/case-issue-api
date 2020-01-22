package gov.usds.case_issues.model;

import gov.usds.case_issues.db.model.BatchUpdateRequestErrors;

public class BatchUpdateRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private BatchUpdateRequestErrors errors;

	public BatchUpdateRequestException(BatchUpdateRequestErrors error) {
		super("Request contained receipt numbers that could not be updated as requested.");
		this.errors = error;
	}

	public BatchUpdateRequestErrors getErrors() {
		return errors;
	}
}
