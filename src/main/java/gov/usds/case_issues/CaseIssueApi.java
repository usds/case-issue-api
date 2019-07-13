package gov.usds.case_issues;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import gov.usds.case_issues.services.CsvLoader;

@SpringBootApplication
public class CaseIssueApi {

	private static final Logger LOG = LoggerFactory.getLogger(CaseIssueApi.class);

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}

	@Bean
	@Profile({"dev","local"})
	public CommandLineRunner loadSampleData(CsvLoader loader, @Value("${sample_data.filename}") String fileName)
			throws IOException {
		LOG.info("Loading file {}", fileName);
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		File dataFile = new File(fileName);
		return args -> {
			MappingIterator<Map<String,String>> values = new CsvMapper()
				.readerFor(Map.class)
				.with(schema)
				.readValues(dataFile);
			loader.loadAll(values);
		};
	}
}
