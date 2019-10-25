package gov.usds.case_issues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CaseIssueApi {

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}
}
