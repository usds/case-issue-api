package gov.usds.case_issues.services;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import gov.usds.case_issues.config.SampleDataConfig;
import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.config.SampleDataConfig.CaseManagementSystemDefinition;
import gov.usds.case_issues.config.SampleDataConfig.NoteSubtypeDefinition;
import gov.usds.case_issues.config.SampleDataConfig.TaggedResource;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.NoteSubtype;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.NoteSubtypeRepository;

/**
 * Service object for loading sample data primarily for use in a dev environment
 */
@Service
@Transactional
public class SampleDataService {

	@Autowired
	private CaseTypeRepository _caseTypeRepo;
	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private NoteSubtypeRepository _noteSubTypeRepo;

	private static final Logger LOG = LoggerFactory.getLogger(SampleDataService.class);

	public void saveCaseManagementSystems(SampleDataConfig loaderConfig) {
		for (CaseManagementSystemDefinition spec : loaderConfig.getCaseManagementSystems()) {
			LOG.debug("Creating Case Management System {} ({}/{}): URLS are {} and {}", spec.getTag(),
						spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
			CaseManagementSystem entity = new CaseManagementSystem(
				spec.getTag(), spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
				_caseManagementSystemRepo.save(entity);
		}
	}

	public void saveCaseTypes(SampleDataConfig loaderConfig) {
		for (TaggedResource spec : loaderConfig.getCaseTypes()) {
			LOG.debug("Creating Case Type {} ({}/{})", spec.getTag(), spec.getName(), spec.getDescription());
			_caseTypeRepo.save(new CaseType(spec.getTag(), spec.getName(), spec.getDescription()));
		}
	}

	public void saveNoteTypes(SampleDataConfig loaderConfig) {
		for (NoteSubtypeDefinition spec : loaderConfig.getNoteSubtypes()) {
			LOG.debug(
				"Creating Cade Type {} ({}/{}) with noteType {}: URL is {}",
				spec.getTag(), spec.getName(), spec.getDescription(), spec.getNoteType(), spec.getUrlTemplate()
			);
			_noteSubTypeRepo.save(new NoteSubtype(spec.getTag(), spec.getNoteType(), spec.getName(), spec.getDescription(), spec.getUrlTemplate()));
		}
	}

	public void loadSampleDataFromFile(SampleDataConfig loaderConfig, CsvLoader loader) throws IOException {
		for (SampleDataFileSpec fileConfig : loaderConfig.getFiles()) {
			LOG.info("Loading data file {}", fileConfig.getFilename());
			CsvSchema schema = CsvSchema.emptySchema().withHeader();
			File dataFile = new File(fileConfig.getFilename());
			MappingIterator<Map<String,String>> values = new CsvMapper().readerFor(Map.class).with(schema).readValues(dataFile);
			loader.loadAll(values, fileConfig);
		}
	}
}