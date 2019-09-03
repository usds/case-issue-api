package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usds.case_issues.config.SampleDataConfig.ColumnSpec;
import gov.usds.case_issues.config.SampleDataConfig.ColumnType;
import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.projections.CaseIssueSummary;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.test_util.DbTruncator;
import gov.usds.case_issues.test_util.FixtureDataInitializationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"autotest", "servicetest"})
public class CsvLoaderTest {

	@Autowired
	private DbTruncator truncator;
	@Autowired
	private FixtureDataInitializationService dataService;
	@Autowired
	private TroubleCaseRepository caseRepo;
	@Autowired
	private CaseIssueRepository issueRepo;

	@Autowired
	private CsvLoader csvLoader;

	@Rule
	public ExpectedException exceptionMatcher = ExpectedException.none(); 

	@Before
	public void cleanUp() {
		truncator.truncateAll();
	}

	@Test
	public void loadAll_invalidCaseManager_exception() {
		dataService.ensureCaseTypeInitialized("STANDARD", "Kinda standard", "boring");
		exceptionMatcher.expectMessage("Case Management System 'NOPE'");
		Iterator<Map<String, String>> values = Collections.emptyListIterator();
		SampleDataFileSpec fileConfig = new SampleDataFileSpec();
		fileConfig.setCaseManagementSystem("NOPE");
		fileConfig.setCaseType("STANDARD");
		csvLoader.loadAll(values, fileConfig);
	}

	@Test
	public void loadAll_invalidCaseType_exception() {
		dataService.ensureCaseManagementSystemInitialized("DEFAULT", "Default", "default");
		exceptionMatcher.expectMessage("Case Type 'NOPE'");
		Iterator<Map<String, String>> values = Collections.emptyListIterator();
		SampleDataFileSpec fileConfig = new SampleDataFileSpec();
		fileConfig.setCaseManagementSystem("DEFAULT");
		fileConfig.setCaseType("NOPE");
		csvLoader.loadAll(values, fileConfig);
	}

	@Test
	public void loadAll_noData_noException() {
		dataService.ensureCaseManagementSystemInitialized("DEFAULT", "Default", "default");
		dataService.ensureCaseTypeInitialized("STANDARD", "Kinda standard", "boring");
		Iterator<Map<String, String>> values = Collections.emptyListIterator();
		SampleDataFileSpec fileConfig = new SampleDataFileSpec();
		fileConfig.setCaseManagementSystem("DEFAULT");
		fileConfig.setCaseType("STANDARD");
		csvLoader.loadAll(values, fileConfig);
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void loadAll_defaultConfigNoExtraColumns_caseFound() {
		CaseManagementSystem caseManagementSystem =
			dataService.ensureCaseManagementSystemInitialized("DEFAULT", "Default", "default");
		dataService.ensureCaseTypeInitialized("STANDARD", "Kinda standard", "boring");

		Map<String, String> singleCase = new HashMap<>();
		singleCase.put(SampleDataFileSpec.DEFAULT_RECEIPT_NUMBER_KEY, "FAKE123");
		singleCase.put(SampleDataFileSpec.DEFAULT_CREATION_DATE_KEY, "1940-09-30T05:30:31Z");
		singleCase.put("ignoredField", "this should not show up");
		Iterator<Map<String, String>> values = Collections.singletonList(singleCase).iterator();

		SampleDataFileSpec fileConfig = new SampleDataFileSpec();
		fileConfig.setCaseManagementSystem("DEFAULT");
		fileConfig.setCaseType("STANDARD");

		csvLoader.loadAll(values, fileConfig);
		Optional<TroubleCase> found = caseRepo.findByCaseManagementSystemAndReceiptNumber(caseManagementSystem, "FAKE123");
		assertTrue("New case found", found.isPresent());
		TroubleCase troubleCase = found.get();
		assertEquals("No extra data created", 0, troubleCase.getExtraData().size());
		assertEquals(1940, troubleCase.getCaseCreation().getYear());
		assertEquals("FAKE123", troubleCase.getReceiptNumber());
		assertEquals(Month.SEPTEMBER, troubleCase.getCaseCreation().getMonth());
		assertEquals(30, troubleCase.getCaseCreation().getDayOfMonth());
		List<CaseIssueSummary> issuesFound = issueRepo.findAllByIssueCaseOrderByIssueCreated(troubleCase);
		assertEquals(1, issuesFound.size());
		assertEquals("OLD", issuesFound.get(0).getIssueType());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void loadAll_customConfig_caseFound() {
		CaseManagementSystem caseManagementSystem =
			dataService.ensureCaseManagementSystemInitialized("DEFAULT", "Default", "default");
		dataService.ensureCaseTypeInitialized("STANDARD", "Kinda standard", "boring");

		Map<String, String> singleCase = new HashMap<>();
		singleCase.put(SampleDataFileSpec.DEFAULT_RECEIPT_NUMBER_KEY, "WRONG");
		singleCase.put(SampleDataFileSpec.DEFAULT_CREATION_DATE_KEY, "1940-09-30T05:30:31Z");
		singleCase.put("myReceipt", "FAKE456");
		singleCase.put("created", "2010-07-24T06:30:00-04:00");
		singleCase.put("myBoolean", "true");
		singleCase.put("myNumber", "10");
		singleCase.put("translatedField", "translated");

		Iterator<Map<String, String>> values = Collections.singletonList(singleCase).iterator();

		SampleDataFileSpec fileConfig = new SampleDataFileSpec();
		fileConfig.setCaseManagementSystem("DEFAULT");
		fileConfig.setCaseType("STANDARD");
		fileConfig.setReceiptNumberKey("myReceipt");
		fileConfig.setCreationDateKey("created");
		List<ColumnSpec> mutableArrayEek = fileConfig.getExtraDataKeys();
		mutableArrayEek.add(new ColumnSpec("myNumber", null, ColumnType.INTEGER));
		mutableArrayEek.add(new ColumnSpec("myBoolean", null, ColumnType.BOOLEAN));
		mutableArrayEek.add(new ColumnSpec("fancyField", "translatedField", ColumnType.STRING));

		csvLoader.loadAll(values, fileConfig);
		Optional<TroubleCase> found = caseRepo.findByCaseManagementSystemAndReceiptNumber(caseManagementSystem, "FAKE456");
		assertTrue("New case found", found.isPresent());
		TroubleCase troubleCase = found.get();
		Map<String, Object> extraData = troubleCase.getExtraData();
		assertEquals("Extra data created", 3, extraData.size());
		assertEquals(true, extraData.get("myBoolean"));
		assertEquals(10, extraData.get("myNumber"));
		assertEquals("translated", extraData.get("fancyField"));
		assertEquals(2010, troubleCase.getCaseCreation().getYear());
		assertEquals("FAKE456", troubleCase.getReceiptNumber());
		assertEquals(Month.JULY, troubleCase.getCaseCreation().getMonth());
		assertEquals(24, troubleCase.getCaseCreation().getDayOfMonth());
	}
}
