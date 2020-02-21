package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

/**
 * A restricting wrapper for {@link CaseSnooze}, to prevent Jackson from
 * automatically discovering fields and accessors that we do not want to expose
 * in an API call. Could theoretically be rendered unnecessary through
 * appropriate use of Jackson annotations, but experimentally, this resulted in
 * excessive coupling between the persistence layer and the presentation layer.
 */
public class CaseSnoozeSummaryFacade implements CaseSnoozeSummary {

	private CaseSnoozeSummary wrapped;
	private List<AttachmentSummary> notes;
	private SerializedUserInformation user;

	public CaseSnoozeSummaryFacade(Optional<? extends CaseSnooze> optionalWrapped) {
		this(optionalWrapped.get());
	}

	public CaseSnoozeSummaryFacade(CaseSnooze wrapped) {
		this.wrapped = wrapped;
		if(wrapped.getCreationUser() != null) {
			user = new SerializedUserInformation(wrapped.getCreationUser());
		} else {
			user = new SerializedUserInformation(wrapped.getCreatedBy(), "");
		}
	}

	public CaseSnoozeSummaryFacade(CaseSnooze wrapped, List<AttachmentSummary> savedNotes) {
		this(wrapped);
		this.notes = savedNotes;
	}


	public String getSnoozeReason() {
		return wrapped.getSnoozeReason();
	}

	public ZonedDateTime getSnoozeStart() {
		return wrapped.getSnoozeStart();
	}

	public ZonedDateTime getSnoozeEnd() {
		return wrapped.getSnoozeEnd();
	}

	public SerializedUserInformation getUser() {
		return user;
	}

	public List<AttachmentSummary> getNotes() {
		return notes;
	}
}
