package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.config.SampleDataConfig;
import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.config.SampleDataConfig.CaseManagementSystemDefinition;
import gov.usds.case_issues.config.SampleDataConfig.NoteSubtypeDefinition;
import gov.usds.case_issues.config.SampleDataConfig.TaggedResource;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.NoteSubtypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.services.CsvLoader;


public class SampleDataServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private SampleDataService _service;
	@Autowired
	private CaseListService _caseListService;
	@Autowired
	private CaseManagementSystemRepository _systemRepo;
	@Autowired
	private CaseTypeRepository _caseTypeRepo;
	@Autowired
	private NoteSubtypeRepository _subtypeRepository;
	@Autowired
	private TroubleCaseRepository _troubleCaseRespository;
	@Autowired
	private CaseIssueRepository _caseIssueRespository;
	@Autowired
	private CaseSnoozeRepository _caseSnoozeRespository;

	@Before
	public void resetDb() {
		truncateDb();
	}

	@Test
	public void saveSingleCaseManagmentSystem() {
		SampleDataConfig loaderConfig = new SampleDataConfig();
		CaseManagementSystemDefinition systemDefinition = new CaseManagementSystemDefinition();
		systemDefinition.setTag("tag");
		systemDefinition.setName("name");
		systemDefinition.setDescription("description");
		loaderConfig.setCaseManagementSystems(Arrays.asList(systemDefinition));
		_service.saveCaseManagementSystems(loaderConfig);
		assertEquals(_systemRepo.count(), 1);
	}

	@Test
	public void saveSingleCaseType() {
		SampleDataConfig loaderConfig = new SampleDataConfig();
		TaggedResource taggedResource = new TaggedResource();
		taggedResource.setTag("tag");
		taggedResource.setName("name");
		taggedResource.setDescription("description");
		loaderConfig.setCaseTypes(Arrays.asList(taggedResource));
		_service.saveCaseTypes(loaderConfig);
		assertEquals(_caseTypeRepo.count(), 1);
	}

	@Test
	public void saveSingleNoteSubType() {
		SampleDataConfig loaderConfig = new SampleDataConfig();
		NoteSubtypeDefinition noteDefiniton = new NoteSubtypeDefinition();
		noteDefiniton.setTag("tag");
		noteDefiniton.setName("name");
		noteDefiniton.setDescription("description");
		loaderConfig.setNoteSubtypes(Arrays.asList(noteDefiniton));
		_service.saveNoteTypes(loaderConfig);
		assertEquals(_subtypeRepository.count(), 1);
	}


	@Test
	public void loadingDataFromCSV() throws IOException {
		SampleDataConfig loaderConfig = new SampleDataConfig();
		SampleDataFileSpec fileSpec = new SampleDataFileSpec();
		_systemRepo.save(new CaseManagementSystem("a", "a", "a"));
		fileSpec.setCaseManagementSystem("a");
		_caseTypeRepo.save(new CaseType("b", "b", "b"));
		fileSpec.setCaseType("b");
		fileSpec.setFilename("src/test/cases.csv");
		loaderConfig.setFiles(Arrays.asList(fileSpec));
		CsvLoader loader = new CsvLoader(_troubleCaseRespository, _caseIssueRespository, _caseSnoozeRespository, _caseListService);
		_service.loadSampleDataFromFile(loaderConfig, loader);
		assertEquals(_caseIssueRespository.count(), 1);
	}
}
