package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.Optional;

import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;

public class CaseSnoozeSummaryFacade implements CaseSnoozeSummary {

	private CaseSnoozeSummary wrapped;

	public CaseSnoozeSummaryFacade(Optional<? extends CaseSnoozeSummary> optionalWrapped) {
		this(optionalWrapped.get());
	}

	public CaseSnoozeSummaryFacade(CaseSnoozeSummary wrapped) {
		super();
		this.wrapped = wrapped;
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

	public String getSnoozeDetails() {
		return wrapped.getSnoozeDetails();
	}
}
