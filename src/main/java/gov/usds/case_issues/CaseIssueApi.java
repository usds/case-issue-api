package gov.usds.case_issues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableConfigurationProperties
@EnableRetry
@EnableCaching
public class CaseIssueApi {

	public static void main(String[] args) {
		SpringApplication.run(CaseIssueApi.class, args);
	}
}
