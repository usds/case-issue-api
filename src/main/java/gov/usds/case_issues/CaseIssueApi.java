package gov.usds.case_issues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableConfigurationProperties
@EnableRetry
public class CaseIssueApi {

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}
}
