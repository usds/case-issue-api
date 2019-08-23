package gov.usds.case_issues.db.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.test_util.HsqlDbTruncator;

public class CaseManagementSystemRepositoryTest extends CaseIssueApiTestBase {

	private static final String DUMMY_DESC = "The big one";
	private static final String DUMMY_NAME = "Magically Infallible System 2";
	private static final String DUMMY_TAG = "FAKE-SYS";
	private static final String DUMMY_USERNAME = "HelloMyNameIsJoe";

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
	@WithMockUser(username=DUMMY_USERNAME)
	public void save_emptyDatabase_recordFound() {
		Date start = new Date();
		repo.save(new CaseManagementSystem(DUMMY_TAG, DUMMY_NAME, DUMMY_DESC));
		CaseManagementSystem found = repo.findAll().iterator().next();
		assertEquals(DUMMY_TAG, found.getCaseManagementSystemTag());
		assertEquals(DUMMY_NAME, found.getName());
		assertEquals(DUMMY_DESC, found.getDescription());
		assertNotNull(found.getInternalId());
		assertTrue(found.getInternalId().longValue() > 0);
		assertEquals(DUMMY_USERNAME, found.getCreatedBy());
		assertTrue(start.toInstant().isBefore(found.getCreatedAt().toInstant()));
		assertTrue(new Date().toInstant().isAfter(found.getCreatedAt().toInstant()));
	}
}
