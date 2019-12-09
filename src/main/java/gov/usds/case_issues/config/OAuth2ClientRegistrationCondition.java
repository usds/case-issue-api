package gov.usds.case_issues.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition implementation to enable definition of beans that are only registered
 * when there is an OAuth2 client registration in the configuration.
 */
public class OAuth2ClientRegistrationCondition extends SpringBootCondition {

	private static final Logger LOG = LoggerFactory.getLogger(OAuth2ClientRegistrationCondition.class);

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean clientPropertiesFound = Binder.get(context.getEnvironment())
			.bind("spring.security.oauth2.client", OAuth2ClientProperties.class)
			.isBound();
		LOG.debug("OAuth2 Client Registration condition result is {}", clientPropertiesFound);
		return clientPropertiesFound ? ConditionOutcome.match() : ConditionOutcome.noMatch("No client properties found");
	}
}
