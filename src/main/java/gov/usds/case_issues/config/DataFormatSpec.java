package gov.usds.case_issues.config;

import java.time.format.DateTimeFormatter;


/**
 * Information about case for conversion during import (mostly the columns/fields
 * expected in each row/object.)
 */
public class DataFormatSpec {

	public static final String DEFAULT_CREATION_DATE_KEY = "creationDate";
	public static final String DEFAULT_RECEIPT_NUMBER_KEY = "receiptNumber";
	public static final DateTimeFormatter DEFAULT_DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

	private String creationDateFormat;
	private String receiptNumberKey = DEFAULT_RECEIPT_NUMBER_KEY;
	private String creationDateKey = DEFAULT_CREATION_DATE_KEY;

	private DateTimeFormatter creationDateParser = null;

	// fancy getter
	public DateTimeFormatter getCreationDateParser() {
		if (creationDateParser == null) {
			if (creationDateFormat != null) {
				creationDateParser = DateTimeFormatter.ofPattern(creationDateFormat);
			} else {
				creationDateParser = DEFAULT_DATETIME_FORMAT;
			}
		}
		return creationDateParser;
	}

	// dumb getters
	public String getCreationDateKey() {
		return creationDateKey;
	}

	public String getCreationDateFormat() {
		return creationDateFormat;
	}

	public String getReceiptNumberKey() {
		return receiptNumberKey;
	}

	// dumb setters
	public void setCreationDateKey(String caseCreationDateKey) {
		this.creationDateKey = caseCreationDateKey;
	}

	public void setCreationDateFormat(String creation_date_format) {
		this.creationDateFormat = creation_date_format;
	}

	public void setReceiptNumberKey(String receiptNumberKey) {
		this.receiptNumberKey = receiptNumberKey;
	}

}
