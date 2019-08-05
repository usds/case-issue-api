package gov.usds.case_issues.config;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import gov.usds.case_issues.config.SampleDataConfig.ColumnSpec;

/**
 * Container for configuration of a single CSV or JSON file to be loaded at startup, including the file path
 * and required information about the columns/fields expected in each row/object.
 */
public class SampleDataFileSpec {
	public static final String DEFAULT_CREATION_DATE_KEY = "creationDate";
	public static final String DEFAULT_RECEIPT_NUMBER_KEY = "receiptNumber";

	private String filename;
	private String creationDateFormat;
	private String receiptNumberKey = DEFAULT_RECEIPT_NUMBER_KEY;
	private String creationDateKey = DEFAULT_CREATION_DATE_KEY;
	private String caseManagementSystem;
	private String caseType;
	private List<ColumnSpec> extraDataKeys = new ArrayList<>();

	/* fancy accessor(s) */
	public DateTimeFormatter getCreationDateParser() {
		if (null != creationDateFormat) {
			return DateTimeFormatter.ofPattern(creationDateFormat);
		}
		return DateTimeFormatter.ISO_DATE_TIME;
	}

	/* dumb accessors */
	public String getCreationDateKey() {
		return creationDateKey;
	}

	public String getCreationDateFormat() {
		return creationDateFormat;
	}

	public List<ColumnSpec> getExtraDataKeys() {
		return extraDataKeys;
	}

	public String getFilename() {
		return filename;
	}

	public String getReceiptNumberKey() {
		return receiptNumberKey;
	}

	public String getCaseManagementSystem() {
		return caseManagementSystem;
	}

	public String getCaseType() {
		return caseType;
	}

	/* dumb setters */
	public void setCaseManagementSystem(String caseManagementSystem) {
		this.caseManagementSystem = caseManagementSystem;
	}

	public void setCaseType(String caseType) {
		this.caseType = caseType;
	}

	public void setCreationDateKey(String caseCreationDateKey) {
		this.creationDateKey = caseCreationDateKey;
	}

	public void setCreationDateFormat(String creation_date_format) {
		this.creationDateFormat = creation_date_format;
	}

	public void setExtraDataKeys(List<ColumnSpec> extraDataKeys) {
		this.extraDataKeys = extraDataKeys;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setReceiptNumberKey(String receiptNumberKey) {
		this.receiptNumberKey = receiptNumberKey;
	}
}
