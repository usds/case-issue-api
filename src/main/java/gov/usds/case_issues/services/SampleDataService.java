package gov.usds.case_issues.services;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(SampleDataService.class);

	public void saveCaseManagementSystems(SampleDataConfig loaderConfig, CaseManagementSystemRepository systemRepo) {
		for (CaseManagementSystemDefinition spec : loaderConfig.getCaseManagementSystems()) {
			LOG.debug("Creating Case Management System {} ({}/{}): URLS are {} and {}", spec.getTag(),
						spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
			CaseManagementSystem entity = new CaseManagementSystem(
				spec.getTag(), spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
			systemRepo.save(entity);
		}
	}

	public void saveCaseTypes(SampleDataConfig loaderConfig, CaseTypeRepository typeRepo) {
		for (TaggedResource spec : loaderConfig.getCaseTypes()) {
			LOG.debug("Creating Case Type {} ({}/{})", spec.getTag(), spec.getName(), spec.getDescription());
			typeRepo.save(new CaseType(spec.getTag(), spec.getName(), spec.getDescription()));
		}
	}

	public void saveNoteTypes(SampleDataConfig loaderConfig, NoteSubtypeRepository subtypeRepo) {
		for (NoteSubtypeDefinition spec : loaderConfig.getNoteSubtypes()) {
			LOG.debug(
				"Creating Cade Type {} ({}/{}) with noteType {}: URL is {}",
				spec.getTag(), spec.getName(), spec.getDescription(), spec.getNoteType(), spec.getUrlTemplate()
			);
			subtypeRepo.save(new NoteSubtype(spec.getTag(), spec.getNoteType(), spec.getName(), spec.getDescription(), spec.getUrlTemplate()));
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