package gov.usds.case_issues.db.model;

import java.util.ArrayList;
import java.util.List;

public class BatchUpdateRequestErrors {

	private List<String> missingReceipts = new ArrayList<>();
	private List<String> invalidCaseTypeReceipts = new ArrayList<>();
	private List<String> ineligibleForActionReceipts = new ArrayList<>();

	public List<String> getMissingReceipts() {
		return missingReceipts;
	}
	public List<String> getInvalidCaseTypeReceipts() {
		return invalidCaseTypeReceipts;
	}
	public List<String> getIneligibleForActionReceipts() {
		return ineligibleForActionReceipts;
	}

	public void missingReceipt(String receiptNumber) {
		missingReceipts.add(receiptNumber);
	}

	public void wrongCaseTypeReceipt(String receiptNumber) {
		invalidCaseTypeReceipts.add(receiptNumber);
	}

	public void ineligibleReceipt(String receiptNumber) {
		ineligibleForActionReceipts.add(receiptNumber);
	}

	public boolean hasErrors() {
		return !(missingReceipts.isEmpty() && invalidCaseTypeReceipts.isEmpty() && ineligibleForActionReceipts.isEmpty());
	}
}
