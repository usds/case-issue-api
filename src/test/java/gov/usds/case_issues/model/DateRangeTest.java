package gov.usds.case_issues.model;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;

import org.junit.Test;

@SuppressWarnings("checkstyle:MagicNumber")
public class DateRangeTest {

	@Test(expected=IllegalArgumentException.class)
	public void constructDateTime_outOfOrder_exception() {
		new DateRange(ZonedDateTime.now(), ZonedDateTime.now().minusDays(1));
	}

	public void constructDateTime_inOrder_ok() {
		ZonedDateTime start = ZonedDateTime.now().minusDays(10);
		ZonedDateTime end = start.minusDays(1);
		DateRange r = new DateRange(start, end);
		assertEquals(start, r.getStartDate());
		assertEquals(end, r.getEndDate());
	}

	public void constructDateTime_openRange_endsNow() {
		ZonedDateTime start = ZonedDateTime.now().minusDays(10);
		ZonedDateTime now = ZonedDateTime.now();
		DateRange r = new DateRange(start, null);
		assertNotNull(r.getEndDate());
		// Clunky near-equality check to avoid one in a million random test failures
		assertFalse(r.getEndDate().isBefore(now));
		assertFalse(r.getEndDate().isAfter(ZonedDateTime.now()));
	}
}
