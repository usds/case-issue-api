package gov.usds.case_issues.test_util;

import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import gov.usds.case_issues.config.WebConfigurationProperties;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseIssueUploadRepository;

/**
 * Configure any mocks that are needed for test cases that involve changing the behavior of an autowired bean from
 * test case to test case. Configuration that is consistent for all autotests should go into application-autotest.yml
 * or otherwise be attached to the "autotest" profile as an autowired bean.
 */
@Configuration
public class MockConfig {

	private static final Logger LOG = LoggerFactory.getLogger(MockConfig.class);

	/**
	 * A profile that provides a mock of the {@link WebConfigurationProperties} object, to allow
	 * easier testing the behavior of things that depend on the values stored therein.
	 */
	public static final String MOCK_PROPERTIES_PROFILE = "mock-properties";
	/**
	 * A profile that provides wrapped versions of some or all Spring Data JPA repositories,
	 * to test (roughly) database interactions failures.
	 * Add additional repositories to this as needed (should not be needed often).
	 */
	public static final String WRAPPED_REPOSITORIES_PROFILE = "mock-repositories";

	@Bean
	@Primary
	@Profile(MOCK_PROPERTIES_PROFILE)
	public WebConfigurationProperties getMockProperties() {
		return Mockito.mock(WebConfigurationProperties.class);
	}

	@Bean
	@Primary
	@Profile(WRAPPED_REPOSITORIES_PROFILE)
	public CaseIssueRepository getIssueRepo(CaseIssueRepository repo) {
		// see also https://github.com/spring-projects/spring-boot/issues/7033
		LOG.info("Wiring up wrapper around {}", repo);
		return Mockito.mock(CaseIssueRepository.class, AdditionalAnswers.delegatesTo(repo));
	}

	@Bean
	@Primary
	@Profile(WRAPPED_REPOSITORIES_PROFILE)
	public CaseIssueUploadRepository getUploadRepo(CaseIssueUploadRepository repo) {
		// see also https://github.com/spring-projects/spring-boot/issues/7033
		LOG.info("Wiring up wrapper around {}", repo);
		return Mockito.mock(CaseIssueUploadRepository.class, AdditionalAnswers.delegatesTo(repo));
	}
}
