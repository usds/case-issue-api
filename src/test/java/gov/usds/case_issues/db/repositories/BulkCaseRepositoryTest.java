package gov.usds.case_issues.db.repositories;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Collections;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

/**
 * Largely argument-validation tests (largely for the date-range filter) for the bulk case 
 * fetch repository methods.
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class BulkCaseRepositoryTest extends CaseIssueApiTestBase {

	private static final ZonedDateTime MONTH_AGO = ZonedDateTime.now().minusMonths(1);
	private static final ZonedDateTime YEAR_AGO = ZonedDateTime.now().minusYears(1);
	private static final ZonedDateTime TOMORROW = ZonedDateTime.now().plusDays(1);
	private static final ZonedDateTime NEXT_WEEK = ZonedDateTime.now().plusDays(7);

	@Autowired
	private BulkCaseRepository _repo;

	@Before
	public void resetDb() {
		truncateDb();
		
	}

	@Test
	public void getActiveCases_plausibleArgs_emptyList() {
		assertEquals(Collections.emptyList(), _repo.getActiveCases(1L, 2L, 3));
		assertEquals(Collections.emptyList(), _repo.getActiveCases(1L, 2L, YEAR_AGO, MONTH_AGO, 3));
		assertEquals(Collections.emptyList(), _repo.getActiveCases(1L, 2L, YEAR_AGO, ZonedDateTime.now(), 3));
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_futureDateStart_error() {
		_repo.getActiveCases(1L, 2L, NEXT_WEEK, MONTH_AGO, 3);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_futureDateEnd_error() {
		_repo.getActiveCases(1L, 2L, MONTH_AGO, NEXT_WEEK, 3);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCases_futureDateBoth_error() {
		_repo.getActiveCases(1L, 2L, TOMORROW, NEXT_WEEK, 3);
	}

	@Test
	public void getActiveCasesAfter_plausibleArgs_emptyList() {
		assertEquals(Collections.emptyList(), _repo.getActiveCasesAfter(1L, 2L, YEAR_AGO, 4L, 3));
		assertEquals(Collections.emptyList(), _repo.getActiveCasesAfter(1L, 2L, YEAR_AGO, 4L, YEAR_AGO, MONTH_AGO, 3));
		assertEquals(Collections.emptyList(), _repo.getActiveCasesAfter(1L, 2L, YEAR_AGO, 4L, YEAR_AGO, ZonedDateTime.now(), 3));
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCasesAfter_futureDateStart_error() {
		_repo.getActiveCasesAfter(1L, 2L, YEAR_AGO, 4L, NEXT_WEEK, MONTH_AGO, 3);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void getActiveCasesAfter_futureDateEnd_error() {
		_repo.getActiveCasesAfter(1L, 2L,YEAR_AGO, 4L, MONTH_AGO, NEXT_WEEK, 3);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getActiveCasesAfter_futureDateBoth_error() {
		_repo.getActiveCasesAfter(1L, 2L, YEAR_AGO, 4L, TOMORROW, NEXT_WEEK, 3);
	}

	@Test
	public void getPreviouslySnoozedCases_plausibleArgs_emptyList() {
		assertEquals(Collections.emptyList(), _repo.getPreviouslySnoozedCases(1L, 2L, 3));
		assertEquals(Collections.emptyList(), _repo.getPreviouslySnoozedCases(1L, 2L, YEAR_AGO, MONTH_AGO, 3));
		assertEquals(Collections.emptyList(), _repo.getPreviouslySnoozedCases(1L, 2L, YEAR_AGO, ZonedDateTime.now(), 3));
	}

	@Test(expected=ConstraintViolationException.class)
	public void getPreviouslySnoozedCases_futureDateStart_error() {
		_repo.getPreviouslySnoozedCases(1L, 2L, NEXT_WEEK, MONTH_AGO, 3);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void getPreviouslySnoozedCases_futureDateEnd_error() {
		_repo.getPreviouslySnoozedCases(1L, 2L, MONTH_AGO, NEXT_WEEK, 3);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getPreviouslySnoozedCases_futureDateBoth_error() {
		_repo.getPreviouslySnoozedCases(1L, 2L, TOMORROW, NEXT_WEEK, 3);
	}

	@Test
	public void getPreviouslySnoozedCasesAfter_plausibleArgs_emptyList() {
		assertEquals(Collections.emptyList(), _repo.getPreviouslySnoozedCasesAfter(1L, 2L, YEAR_AGO, 4L, 3));
		assertEquals(Collections.emptyList(), _repo.getPreviouslySnoozedCasesAfter(1L, 2L, YEAR_AGO, 4L, YEAR_AGO, MONTH_AGO, 3));
		assertEquals(Collections.emptyList(), _repo.getPreviouslySnoozedCasesAfter(1L, 2L, YEAR_AGO, 4L, YEAR_AGO, ZonedDateTime.now(), 3));
	}

	@Test(expected=ConstraintViolationException.class)
	public void getPreviouslySnoozedCasesAfter_futureDateStart_error() {
		_repo.getPreviouslySnoozedCasesAfter(1L, 2L, YEAR_AGO, 4L, NEXT_WEEK, MONTH_AGO, 3);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void getPreviouslySnoozedCasesAfter_futureDateEnd_error() {
		_repo.getPreviouslySnoozedCasesAfter(1L, 2L,YEAR_AGO, 4L, MONTH_AGO, NEXT_WEEK, 3);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getPreviouslySnoozedCasesAfter_futureDateBoth_error() {
		_repo.getPreviouslySnoozedCasesAfter(1L, 2L, YEAR_AGO, 4L, TOMORROW, NEXT_WEEK, 3);
	}

	@Test
	public void getSnoozedCases_plausibleArgs_emptyList() {
		assertEquals(Collections.emptyList(), _repo.getSnoozedCases(1L, 2L, 3));
		assertEquals(Collections.emptyList(), _repo.getSnoozedCases(1L, 2L, YEAR_AGO, MONTH_AGO, 3));
		assertEquals(Collections.emptyList(), _repo.getSnoozedCases(1L, 2L, YEAR_AGO, ZonedDateTime.now(), 3));
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_futureDateStart_error() {
		_repo.getSnoozedCases(1L, 2L, NEXT_WEEK, MONTH_AGO, 3);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_futureDateEnd_error() {
		_repo.getSnoozedCases(1L, 2L, MONTH_AGO, NEXT_WEEK, 3);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCases_futureDateBoth_error() {
		_repo.getSnoozedCases(1L, 2L, TOMORROW, NEXT_WEEK, 3);
	}

	@Test
	public void getSnoozedCasesAfter_plausibleArgs_emptyList() {
		assertEquals(Collections.emptyList(), _repo.getSnoozedCasesAfter(1L, 2L, YEAR_AGO, MONTH_AGO, 4L, 3));
		assertEquals(Collections.emptyList(), _repo.getSnoozedCasesAfter(1L, 2L, YEAR_AGO, MONTH_AGO, 4L, YEAR_AGO, MONTH_AGO, 3));
		assertEquals(Collections.emptyList(), _repo.getSnoozedCasesAfter(1L, 2L, YEAR_AGO, MONTH_AGO, 4L, YEAR_AGO, ZonedDateTime.now(), 3));
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCasesAfter_futureDateStart_error() {
		_repo.getSnoozedCasesAfter(1L, 2L, YEAR_AGO, MONTH_AGO, 4L, NEXT_WEEK, MONTH_AGO, 3);
	}
	
	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCasesAfter_futureDateEnd_error() {
		_repo.getSnoozedCasesAfter(1L, 2L,YEAR_AGO, MONTH_AGO, 4L, MONTH_AGO, NEXT_WEEK, 3);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getSnoozedCasesAfter_futureDateBoth_error() {
		_repo.getSnoozedCasesAfter(1L, 2L, YEAR_AGO, MONTH_AGO, 4L, TOMORROW, NEXT_WEEK, 3);
	}	
}
