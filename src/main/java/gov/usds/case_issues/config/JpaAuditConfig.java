package gov.usds.case_issues.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@EnableJpaAuditing(
	auditorAwareRef=JpaAuditConfig.AUDITOR
)
@Configuration
public class JpaAuditConfig {

	static final String AUDITOR = "auditorSourceBean";

	@Bean(AUDITOR)
	public AuditorAware<String> getCurrentIdProvider() {
		return () -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			boolean hasUser = authentication != null && authentication.isAuthenticated();
			return hasUser ? Optional.of(authentication.getName()) : Optional.empty();
		};
	}
}
