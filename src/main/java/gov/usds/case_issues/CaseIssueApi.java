package gov.usds.case_issues;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import gov.usds.case_issues.config.SampleDataConfig;
import gov.usds.case_issues.config.SampleDataConfig.CaseManagementSystemDefinition;
import gov.usds.case_issues.config.SampleDataConfig.TaggedResource;
import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.services.CsvLoader;

@SpringBootApplication
public class CaseIssueApi {

	private static final Logger LOG = LoggerFactory.getLogger(CaseIssueApi.class);

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}

	@Bean
	@Profile({"dev"})
	public CommandLineRunner loadSampleData(CsvLoader loader, SampleDataConfig loaderConfig,
				CaseManagementSystemRepository systemRepo, CaseTypeRepository typeRepo)
			throws IOException {
		return args -> {
			for (CaseManagementSystemDefinition spec : loaderConfig.getCaseManagementSystems()) {
				LOG.debug("Creating Case Management System {} ({}/{}): URLS are {} and {}", spec.getTag(),
						spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
				CaseManagementSystem entity = new CaseManagementSystem(
					spec.getTag(), spec.getName(), spec.getDescription(), spec.getApplicationUrl(), spec.getCaseDetailsUrlTemplate());
				systemRepo.save(entity);
			}
			for (TaggedResource spec : loaderConfig.getCaseTypes()) {
				LOG.debug("Creating Case Type {} ({}/{})", spec.getTag(), spec.getName(), spec.getDescription());
				typeRepo.save(new CaseType(spec.getTag(), spec.getName(), spec.getDescription()));
			}
			for (SampleDataFileSpec fileConfig : loaderConfig.getFiles()) {
				LOG.info("Loading data file {}", fileConfig.getFilename());
				CsvSchema schema = CsvSchema.emptySchema().withHeader();
				File dataFile = new File(fileConfig.getFilename());
				MappingIterator<Map<String,String>> values = new CsvMapper()
						.readerFor(Map.class)
						.with(schema)
						.readValues(dataFile);
				loader.loadAll(values, fileConfig);
			}
		};
	}
}
