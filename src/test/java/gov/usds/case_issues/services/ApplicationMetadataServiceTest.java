package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.model.NavigationEntry;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class ApplicationMetadataServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private ApplicationMetadataService _service;

	@Before
	public void resetDb() {
		truncateDb();
	}

	@Test
	public void getCaseNavigation_emptyDb_emptyResult() {
		List<NavigationEntry> nav = _service.getCaseNavigation();
		assertNotNull(nav);
		assertEquals(0, nav.size());
	}

	@Test
	public void getCaseNavigation_typesWithoutSystems_emptyResult() {
		initTypes();
		List<NavigationEntry> nav = _service.getCaseNavigation();
		assertNotNull(nav);
		assertEquals(0, nav.size());
	}

	@Test
	public void getCaseNavigation_systemsWithoutTypes_resultsWithoutCases() {
		initSystems();
		List<NavigationEntry> nav = _service.getCaseNavigation();
		assertNotNull(nav);
		assertEquals(2, nav.size());
		assertEquals("Silly system", nav.get(0).getName());
		assertEquals("You and me system", nav.get(1).getName());
		assertFalse("No types found for first entry", nav.get(0).getCaseTypes().iterator().hasNext());
		assertFalse("No types found for second entry", nav.get(1).getCaseTypes().iterator().hasNext());
	}

	@Test
	public void getCaseNavigation_typesAndSystems_allSystemsHaveTypes() {
		initTypes();
		initSystems();
		List<NavigationEntry> nav = _service.getCaseNavigation();
		assertNotNull(nav);
		assertEquals(2, nav.size());
		assertEquals("Silly system", nav.get(0).getName());
		assertEquals("You and me system", nav.get(1).getName());
		Set<String> typeTags = new HashSet<>();
		nav.get(0).getCaseTypes().forEach(t -> typeTags.add(t.getExternalId()));
		assertEquals("two types found", 2, typeTags.size());
		assertTrue("FOO found", typeTags.contains("FOO"));
		assertTrue("BAR found", typeTags.contains("FOO"));
		typeTags.clear();
		nav.get(1).getCaseTypes().forEach(t -> typeTags.add(t.getExternalId()));
		assertEquals("two types found", 2, typeTags.size());
		assertTrue("FOO found", typeTags.contains("FOO"));
		assertTrue("BAR found", typeTags.contains("FOO"));
	}

	private void initTypes() {
		_dataService.ensureCaseTypeInitialized("FOO", "Foo Case", "Where's the foo?");
		_dataService.ensureCaseTypeInitialized("BAR", "Bar Case", "Where's the bar?");
	}

	private void initSystems() {
		_dataService.ensureCaseManagementSystemInitialized("ABC", "Silly system", null);
		_dataService.ensureCaseManagementSystemInitialized("123", "You and me system", null);
	}
}
