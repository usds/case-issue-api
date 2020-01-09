package gov.usds.case_issues.db.model.reporting;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import gov.usds.case_issues.db.model.TroubleCaseFixedData;

@Entity
@Table(name="filterable_case_view")
@Immutable
public class FilterableCase extends TroubleCaseFixedData {

    @Column(name="extra_data_converted")
    private Map<String, Object> extraData;

    // snooze embeds
    private String snoozeReason;
    private ZonedDateTime snoozeStart;
    private ZonedDateTime snoozeEnd;

    // state
    private boolean hasOpenIssue;

	public Map<String, Object> getExtraData() {
		return extraData;
	}

	public String getSnoozeReason() {
		return snoozeReason;
	}

	public ZonedDateTime getSnoozeStart() {
		return snoozeStart;
	}

	public ZonedDateTime getSnoozeEnd() {
		return snoozeEnd;
	}

	public boolean isHasOpenIssue() {
		return hasOpenIssue;
	}
}
