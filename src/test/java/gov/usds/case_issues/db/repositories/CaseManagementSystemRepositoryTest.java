package gov.usds.case_issues.db.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usds.case_issues.db.model.CaseManagementSystem;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseManagementSystemRepositoryTest {

	private static final String DUMMY_DESC = "The big one";
	private static final String DUMMY_NAME = "Magically Infallible System 2";
	private static final String DUMMY_TAG = "FAKE-SYS";

	@Autowired
	private CaseManagementSystemRepository repo;
	
	@Test
	public void findAll_emptyDatabase_emptyResult() {
		Iterable<CaseManagementSystem> found = repo.findAll();
		assertNotNull(found);
		assertFalse(found.iterator().hasNext());
	}

	@Test
	public void save_emptyDatabase_recordFound() {
		repo.save(new CaseManagementSystem(DUMMY_TAG, DUMMY_NAME, DUMMY_DESC));
		CaseManagementSystem found = repo.findAll().iterator().next();
		assertEquals(DUMMY_TAG, found.getCaseManagementSystemTag());
		assertEquals(DUMMY_NAME, found.getName());
		assertEquals(DUMMY_DESC, found.getDescription());
		assertNotNull(found.getCaseManagementSystemId());
		assertTrue(found.getCaseManagementSystemId().longValue() > 0);
	}
}
