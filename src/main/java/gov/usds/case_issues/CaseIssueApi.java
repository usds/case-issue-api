package gov.usds.case_issues;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import gov.usds.case_issues.config.SampleDataConfig;
import gov.usds.case_issues.config.SampleDataConfig.NoteSubtypeDefinition;
import gov.usds.case_issues.config.SampleDataConfig.CaseManagementSystemDefinition;
import gov.usds.case_issues.config.SampleDataConfig.TaggedResource;
import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.NoteSubtype;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.NoteSubtypeRepository;
import gov.usds.case_issues.services.CsvLoader;

@SpringBootApplication
@EnableConfigurationProperties
public class CaseIssueApi {

	private static final Logger LOG = LoggerFactory.getLogger(CaseIssueApi.class);

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}

	@Bean
	@Profile({"dev"})
	public CommandLineRunner loadSampleData(CsvLoader loader, SampleDataConfig loaderConfig,
				CaseManagementSystemRepository systemRepo, CaseTypeRepository typeRepo, NoteSubtypeRepository subtypeRepo)
			throws IOException {
		return args -> {
			for (CaseManagementSystemDefinition spec : loaderConfig.getCaseManagementSystems()) {
				CaseManagementSystem entity = new CaseManagementSystem(
					spec.getTag(), spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
				systemRepo.save(entity);
			}
			for (TaggedResource spec : loaderConfig.getCaseTypes()) {
				typeRepo.save(new CaseType(spec.getTag(), spec.getName(), spec.getDescription()));
			}
			for (NoteSubtypeDefinition spec : loaderConfig.getNoteSubtypes()) {
				subtypeRepo.save(new NoteSubtype(spec.getTag(), spec.getNoteType(), spec.getName(), spec.getDescription(), spec.getUrlTemplate()));
			}
			for (SampleDataFileSpec fileConfig : loaderConfig.getFiles()) {
				CsvSchema schema = CsvSchema.emptySchema().withHeader();
				File dataFile = new File(fileConfig.getFilename());
				MappingIterator<Map<String,String>> values = new CsvMapper().readerFor(Map.class).with(schema).readValues(dataFile);
				loader.loadAll(values, fileConfig);
			}
		};
	}
}
