package gov.usds.case_issues.model;

import java.util.List;
import java.util.Optional;

public class BatchUpdateRequest {

	private BatchUpdateAction updateAction;
	private List<String> receiptNumbers;
	private Optional<String> snoozeReason;
	private List<AttachmentRequest> attachments;
	private Optional<Integer> duration;

	public BatchUpdateRequest() {

	}

	public BatchUpdateRequest(BatchUpdateAction updateAction, List<String> receiptNumbers,
			List<AttachmentRequest> attachments, Optional<String> snoozeReason, int duration) {
		super();
		this.updateAction = updateAction;
		this.receiptNumbers = receiptNumbers;
		this.attachments = attachments;
		this.snoozeReason = snoozeReason;
		this.duration = Optional.of(duration);
	}

	public BatchUpdateAction getUpdateAction() {
		return updateAction;
	}

	public List<String> getReceiptNumbers() {
		return receiptNumbers;
	}
	public Optional<String> getSnoozeReason() {
		return snoozeReason;
	}

	public List<AttachmentRequest> getAttachments() {
		return attachments;
	}

	public Optional<Integer> getDuration() {
		return duration;
	}
}
