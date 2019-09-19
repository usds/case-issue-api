package gov.usds.case_issues.test_util;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gov.usds.case_issues.config.WebConfigurationProperties;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Configure any mocks that are needed for test cases that involve changing the behavior of an autowired bean from
 * test case to test case. Configuration that is consistent for all autotests should go into application-autotest.yml
 * or otherwise be attached to the "autotest" profile as an autowired bean.
 */
@Configuration
public class MockConfig {

	@Bean
	@Primary
	@Profile("mock-properties")
	public WebConfigurationProperties getMockProperties() {
		return Mockito.mock(WebConfigurationProperties.class);
	}
}
