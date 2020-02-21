package gov.usds.case_issues.services.model;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.model.reporting.FilterableCase;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseSummary;

/**
 * A delegating façade that implements the {@link CaseSummary} interface by wrapping a {@link FilterableCase}. 
 */
public class DelegatingFilterableCaseSummary implements CaseSummary {

	private FilterableCase _root;
	private List<AttachmentSummary> _attachments;
	private CaseSnoozeSummary _snooze;

	public DelegatingFilterableCaseSummary(FilterableCase r, List<AttachmentSummary> attachments) {
		_root = r;
		_attachments = attachments;
		if (attachments == null) {
			_attachments = Collections.emptyList();
		}
		if (_root.getSnoozeStart() != null) {
			_snooze = new DelegatingSnoozeSummary();
		}
	}

	@Override
	public String getReceiptNumber() {
		return _root.getReceiptNumber();
	}
	@Override
	public ZonedDateTime getCaseCreation() {
		return _root.getCaseCreation();
	}
	@Override
	public Map<String, Object> getExtraData() {
		return _root.getExtraData();
	}
	@Override
	public ZonedDateTime getCaseDataModifiedDate() {
		return ZonedDateTime.ofInstant(_root.getUpdatedAt().toInstant(), GMT);
	}
	@Override
	public ZonedDateTime getCaseInitialUploadDate() {
		return ZonedDateTime.ofInstant(_root.getCreatedAt().toInstant(), GMT);
	}
	@Override
	public CaseSnoozeSummary getSnoozeInformation() {
		return _snooze;
	}
	@Override
	public List<AttachmentSummary> getNotes() {
		return _attachments;
	}

	/**
	 * A delegating wrapper that implements {@link CaseSnooozeSummary} by wrapping the same root
	 * {@link FilterableCase} as the parent object.
	 */
	private final class DelegatingSnoozeSummary implements CaseSnoozeSummary {
		@Override
		public ZonedDateTime getSnoozeStart() {
			return _root.getSnoozeStart();
		}

		@Override
		public String getSnoozeReason() {
			return _root.getSnoozeReason();
		}

		@Override
		public ZonedDateTime getSnoozeEnd() {
			return _root.getSnoozeEnd();
		}
	}
}