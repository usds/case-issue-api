package gov.usds.case_issues;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import gov.usds.case_issues.config.SampleDataConfig;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.NoteSubtypeRepository;
import gov.usds.case_issues.services.CsvLoader;
import gov.usds.case_issues.services.SampleDataService;

@SpringBootApplication
@EnableConfigurationProperties
public class CaseIssueApi {

	@Autowired
	private SampleDataService _sampleDataService;

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}

	@Bean
	@Profile({"dev"})
	public CommandLineRunner loadSampleData(CsvLoader loader, SampleDataConfig loaderConfig,
				CaseManagementSystemRepository systemRepo, CaseTypeRepository typeRepo, NoteSubtypeRepository subtypeRepo)
			throws IOException {
		return args -> {
			_sampleDataService.saveCaseManagementSystems(loaderConfig, systemRepo);
			_sampleDataService.saveCaseTypes(loaderConfig, typeRepo);
			_sampleDataService.saveNoteTypes(loaderConfig, subtypeRepo);
			_sampleDataService.loadSampleDataFromFile(loaderConfig, loader);
		};
	}
}
