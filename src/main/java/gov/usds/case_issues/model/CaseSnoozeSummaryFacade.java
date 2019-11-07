package gov.usds.case_issues.model;

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
public class CaseSnoozeSummaryFacade extends CaseSnoozeSummary {

	private List<NoteSummary> notes;

	public CaseSnoozeSummaryFacade(Optional<? extends CaseSnooze> optionalWrapped) {
		this(optionalWrapped.get());
	}

	public CaseSnoozeSummaryFacade(CaseSnooze wrapped) {
		super(wrapped);
	}

	public CaseSnoozeSummaryFacade(CaseSnooze wrapped, List<NoteSummary> savedNotes) {
		super(wrapped);
		this.notes = savedNotes;
	}

	public List<NoteSummary> getNotes() {
		return notes;
	}
}
