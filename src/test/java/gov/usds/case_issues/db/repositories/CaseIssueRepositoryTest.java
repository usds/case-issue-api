package gov.usds.case_issues.db.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class CaseIssueRepositoryTest extends CaseIssueApiTestBase {

	private static final String VALID_ISSUE = "RELEVANT";
	private static final String VALID_TYPE = "CT1";
	private static final String VALID_MGT_SYS = "CS1";
	private static final String DUMMY_USERNAME = "IamNotSam";

	@Autowired
	private CaseIssueRepository _repo;

	private CaseManagementSystem _system;
	private CaseType _type;
	private ZonedDateTime _now;

	@Before
	public void reset() {
		truncateDb();
		_now = ZonedDateTime.now();
		_system = _dataService.ensureCaseManagementSystemInitialized(VALID_MGT_SYS, "Anonymouse", null);
		_type = _dataService.ensureCaseTypeInitialized(VALID_TYPE, "Nobody", null);
	}

	@Test(expected=DataIntegrityViolationException.class)
	public void createIssue_uniqueKeyViolation_exception() {
		TroubleCase mainCase = _dataService.initCase(_system, "HELO1234", _type, _now.minusMonths(1));
		_repo.save(new CaseIssue(mainCase, "HEYO", _now.minusMonths(2)));
		CaseIssue conflicting = new CaseIssue(mainCase, "HEYO", _now.minusMonths(2));
		conflicting.setIssueClosed(_now);
		_repo.save(conflicting);
	}

	@Test(expected=DataIntegrityViolationException.class)
	@org.junit.Ignore("Constraint not available on HSQLDB")
	public void createIssue_rangeOverlapViolation_exception() {
		// better: org.junit.Assume.assumeThat... (database is not postgresql)
		TroubleCase mainCase = _dataService.initCase(_system, "HELO1234", _type, _now.minusMonths(1));
		_repo.save(new CaseIssue(mainCase, "HEYO", _now.minusMonths(2)));
		CaseIssue conflicting = new CaseIssue(mainCase, "HEYO", _now.minusMonths(1));
		_repo.save(conflicting);
	}

	@Test
	public void findAllForCase_noIssues_emptyList() {
		ZonedDateTime now = ZonedDateTime.now();
		TroubleCase mainCase = _dataService.initCase(_system, "HELO1234", _type, now.minusMonths(1));
		List<CaseIssueSummary> found = _repo.findAllByIssueCaseOrderByIssueCreated(mainCase);
		assertEquals("Issue list should be empty", 0, found.size());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void findAllForCase_multipleIssues_sortedList() {
		TroubleCase mainCase = _dataService.initCase(_system, "HELO1234", _type, _now.minusMonths(1));
		_dataService.initIssue(mainCase, "AGE", _now.minusDays(10), null);
		_dataService.initIssue(mainCase, "LOST", _now.minusDays(7), _now.minusDays(6));
		_dataService.initIssue(mainCase, "UNKNOWN", _now.minusDays(8), null);
		List<CaseIssueSummary> allIssues = _repo.findAllByIssueCaseOrderByIssueCreated(mainCase);
		assertEquals("Three cases", 3, allIssues.size());
		assertEquals("First issue type", "AGE", allIssues.get(0).getIssueType());
		assertEquals("First issue date", _now.minusDays(10), allIssues.get(0).getIssueCreated());
		assertNull("First issue still open", allIssues.get(0).getIssueClosed());

		assertEquals("Second issue type", "UNKNOWN", allIssues.get(1).getIssueType());
		assertEquals("Second issue date", _now.minusDays(8), allIssues.get(1).getIssueCreated());
		assertNull("Second issue still open", allIssues.get(1).getIssueClosed());

		assertEquals("Third issue type", "LOST", allIssues.get(2).getIssueType());
		assertEquals("Third issue date", _now.minusDays(7), allIssues.get(2).getIssueCreated());
		assertEquals("Third issue closed date", _now.minusDays(6), allIssues.get(2).getIssueClosed());
	}

	@Test
	public void findActiveIssues_noIssues_emptyList() {
		List<CaseIssue> issues = _repo.findActiveIssues(_system, _type, VALID_ISSUE);
		assertEquals(0, issues.size());
	}

	@Test
	public void findActiveIssues_noRelevantIssues_emptyList() {
		ZonedDateTime then = _now.minusWeeks(1);
		CaseManagementSystem c3 = _dataService.ensureCaseManagementSystemInitialized(
				"C3", "Case manager we do not care about", null);
		CaseType t4 = _dataService.ensureCaseTypeInitialized("T4", "Another Type", null);
		TroubleCase relevantCase = _dataService.initCase(_system, "F100", _type, then);
		TroubleCase wrongTypeCase = _dataService.initCase(_system, "F101", t4, then);
		TroubleCase wrongSystemCase = _dataService.initCase(c3, "F102", _type, then);
		_dataService.initOpenIssue(relevantCase, "IRRELEVANT", then);
		_dataService.initOpenIssue(wrongSystemCase, VALID_ISSUE, then);
		_dataService.initOpenIssue(wrongTypeCase, VALID_ISSUE, then);
		_dataService.initIssue(relevantCase, VALID_ISSUE, then, then.plusDays(1));

		List<CaseIssue> issues = _repo.findActiveIssues(_system, _type, VALID_ISSUE);
		assertEquals(0, issues.size());
	}

	@Test
	public void findActiveIssues_relevantAndIrrelevantIssues_correctIssuesFound() {
		ZonedDateTime then = _now.minusWeeks(1);
		CaseManagementSystem c3 = _dataService.ensureCaseManagementSystemInitialized(
				"C3", "Case manager we do not care about", null);
		CaseType t4 = _dataService.ensureCaseTypeInitialized("T4", "Another Type", null);
		TroubleCase relevantCase = _dataService.initCase(_system, "F100", _type, then);
		TroubleCase wrongTypeCase = _dataService.initCase(_system, "F101", t4, then);
		TroubleCase wrongSystemCase = _dataService.initCase(c3, "F102", _type, then);

		_dataService.initOpenIssue(relevantCase, "IRRELEVANT");
		_dataService.initOpenIssue(wrongSystemCase, VALID_ISSUE);
		_dataService.initOpenIssue(wrongTypeCase, VALID_ISSUE);
		_dataService.initIssue(relevantCase, VALID_ISSUE, then, then.plusDays(1));

		TroubleCase r1 = _dataService.initCase(_system, "F103", _type, then);
		_dataService.initOpenIssue(r1, VALID_ISSUE, then.plusDays(2));
		_dataService.initOpenIssue(r1, "IRRELEVANT");

		TroubleCase r2 = _dataService.initCase(_system, "F104", _type, then.plusDays(1));
		_dataService.initOpenIssue(r2, VALID_ISSUE);

		List<CaseIssue> issues = _repo.findActiveIssues(_system, _type, VALID_ISSUE);
		assertEquals("exactly two issues should be found", 2, issues.size());
		issues.sort((a,b)->a.getIssueCreated().compareTo(b.getIssueCreated()));
		// we haven't implemented a custom .equals on these, so we need to compare on a field, not the object
		assertEquals(r2.getReceiptNumber(), issues.get(0).getIssueCase().getReceiptNumber());
		assertEquals(then.plusDays(1), issues.get(0).getIssueCreated());
		assertEquals(r1.getReceiptNumber(), issues.get(1).getIssueCase().getReceiptNumber());
		assertEquals(then.plusDays(2), issues.get(1).getIssueCreated());
	}

	@Test
	public void saveCase_noUser_auditInfoCorrect() {
		Instant startTime = new Date().toInstant();
		TroubleCase c = _dataService.initCase(_system, "ABC1234", _type, _now.minusDays(1));
		CaseIssue saved = _repo.save(new CaseIssue(c, "FAKE", _now.minusHours(1)));
		Instant end = new Date().toInstant();
		assertNull(saved.getCreatedBy());
		assertNull(saved.getUpdatedBy());
		Date createdAt = saved.getCreatedAt();
		assertTrue(startTime.isBefore(createdAt.toInstant()));
		assertTrue(end.isAfter(createdAt.toInstant()));
		assertEquals(createdAt, saved.getUpdatedAt());
	}

	@Test
	@WithMockUser(DUMMY_USERNAME)
	public void saveCaseAndUpdate_withUser_auditInfoCorrect() {
		Instant startTime = new Date().toInstant();
		TroubleCase c = _dataService.initCase(_system, "ABC1234", _type, _now.minusDays(1));
		CaseIssue saved = _repo.save(new CaseIssue(c, "FAKE", _now.minusHours(1)));
		Instant middle = new Date().toInstant();
		assertEquals(DUMMY_USERNAME, saved.getCreatedBy());
		assertEquals(DUMMY_USERNAME, saved.getUpdatedBy());
		Date createdAt = saved.getCreatedAt();
		assertTrue(startTime.isBefore(createdAt.toInstant()));
		assertTrue(middle.isAfter(createdAt.toInstant()));
		assertTrue(startTime.isBefore(saved.getUpdatedAt().toInstant()));
		assertTrue(middle.isAfter(saved.getUpdatedAt().toInstant()));
		saved.setIssueClosed(ZonedDateTime.now());
		CaseIssue resaved = _repo.save(saved);
		Instant end = new Date().toInstant();
		assertEquals(createdAt, resaved.getCreatedAt());
		assertTrue(middle.isBefore(resaved.getUpdatedAt().toInstant()));
		assertTrue(end.isAfter(resaved.getUpdatedAt().toInstant()));
		assertEquals(DUMMY_USERNAME, resaved.getUpdatedBy());
	}
}
