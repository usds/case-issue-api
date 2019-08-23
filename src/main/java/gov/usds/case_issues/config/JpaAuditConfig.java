package gov.usds.case_issues.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;

@EnableJpaAuditing(
	auditorAwareRef=JpaAuditConfig.AUDITOR
)
@Configuration
public class JpaAuditConfig {

	static final String AUDITOR = "auditorSourceBean";

	public static final String FAKE_ID = "HEYYYY";

	@Bean(AUDITOR)
	public AuditorAware<String> getCurrentIdProvider() {
		return () -> {
			Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
			return (authentication != null && authentication.isAuthenticated()) ? Optional.of(authentication.getName()) : Optional.empty();
		};
	}
}
