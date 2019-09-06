package gov.usds.case_issues.db.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.test_util.HsqlDbTruncator;

public class CaseManagementSystemRepositoryTest extends CaseIssueApiTestBase {

	private static final String DUMMY_DESC = "The big one";
	private static final String DUMMY_NAME = "Magically Infallible System 2";
	private static final String DUMMY_TAG = "FAKE-SYS";

	@Autowired
	private CaseManagementSystemRepository repo;
	@Autowired
	private HsqlDbTruncator truncator;

	@Before
	public void emptyDb() {
		truncator.truncateAll();
	}

	@Test
	public void findAll_emptyDatabase_emptyResult() {
		Iterable<CaseManagementSystem> found = repo.findAll();
		assertNotNull(found);
		assertFalse(found.iterator().hasNext());
	}

	@Test
	public void save_emptyDatabaseNoUser_recordFound() {
		Instant startTime = new Date().toInstant();
		repo.save(new CaseManagementSystem(DUMMY_TAG, DUMMY_NAME, DUMMY_DESC));
		CaseManagementSystem found = repo.findAll().iterator().next();
		assertEquals(DUMMY_TAG, found.getExternalId());
		assertEquals(DUMMY_NAME, found.getName());
		assertEquals(DUMMY_DESC, found.getDescription());
		assertNotNull(found.getInternalId());
		assertTrue(found.getInternalId().longValue() > 0);
		assertNull(found.getCreatedBy());
		Instant createdAtInstant = found.getCreatedAt().toInstant();
		assertInstantOrder(startTime, createdAtInstant);
		assertInstantOrder(createdAtInstant, new Date().toInstant());
	}

	private static void assertInstantOrder(Instant before, Instant after) {
		String message = String.format("Validating that %s is before %s", before.toString(), after.toString());
		assertTrue(message, after.isAfter(before));
	}
}
