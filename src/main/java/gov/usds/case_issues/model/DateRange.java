package gov.usds.case_issues.model;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;

/**
 * A simple container object for time/date ranges.
 */
public class DateRange {

	private ZonedDateTime _start;
	private ZonedDateTime _end;

	public DateRange(@PastOrPresent @NotNull ZonedDateTime startTime, @PastOrPresent ZonedDateTime endTime) {
		_start = startTime;
		if (endTime == null) {
			endTime = ZonedDateTime.now();
		}
		if (endTime.isBefore(startTime)) {
			throw new IllegalArgumentException("Range end must be after beginning"); // jerks
		}
		_end = endTime;
	}

	public ZonedDateTime getStartDate() {
		return _start;
	}

	public ZonedDateTime getEndDate() {
		return _end;
	}
}
