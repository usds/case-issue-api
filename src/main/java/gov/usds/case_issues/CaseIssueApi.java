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
	public CommandLineRunner loadSampleData(CsvLoader loader, SampleDataConfig loaderConfig) throws IOException {
		return args -> {
			_sampleDataService.saveCaseManagementSystems(loaderConfig);
			_sampleDataService.saveCaseTypes(loaderConfig);
			_sampleDataService.saveNoteTypes(loaderConfig);
			_sampleDataService.loadSampleDataFromFile(loaderConfig, loader);
		};
	}
}
