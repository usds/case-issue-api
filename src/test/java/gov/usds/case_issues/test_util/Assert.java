package gov.usds.case_issues.test_util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

/**
 * Reusable utility assertions for local tests.
 */
public class Assert {

	public static void assertInstantOrder(Instant before, Instant after, boolean inclusive) {
		String format = inclusive ? "Validating that %s is before or equal to %s" : "Validating that %s is strictly before %s";
		String message = String.format(format, before.toString(), after.toString());
		if (inclusive) {
			assertFalse(message, after.isBefore(before));
		} else {
			assertTrue(message, before.isBefore(after));
		}
	}
}
