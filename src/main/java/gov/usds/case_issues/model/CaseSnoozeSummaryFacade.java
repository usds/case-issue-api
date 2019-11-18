package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.UserInformation;
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
	private List<NoteSummary> notes;
	private String id;
	private String name;

	public CaseSnoozeSummaryFacade(Optional<? extends CaseSnoozeSummary> optionalWrapped) {
		this(optionalWrapped.get());
	}

	public CaseSnoozeSummaryFacade(CaseSnoozeSummary wrapped) {
		super();
		this.wrapped = wrapped;
	}

	public CaseSnoozeSummaryFacade(CaseSnooze wrapped, List<NoteSummary> savedNotes) {
		this(wrapped);
		this.notes = savedNotes;
	}

	public CaseSnoozeSummaryFacade(CaseSnoozeSummary wrapped, UserInformation user) {
		this(wrapped);
		id = user.getId();
		name = user.getPrintName();
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

	@JsonIgnore
	public String getCreatedBy() {
		return id;
	}

	public SerializedUserInformation getUser() {
		return new SerializedUserInformation(id, name);
	}

	public List<NoteSummary> getNotes() {
		return notes;
	}
}
