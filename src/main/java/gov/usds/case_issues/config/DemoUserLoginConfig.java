package gov.usds.case_issues.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import gov.usds.case_issues.config.model.AuthenticationType;
import gov.usds.case_issues.controllers.UserInformationApiController;
import gov.usds.case_issues.services.UserService;
import springfox.documentation.service.ApiInfo;

@Configuration
@Profile({"dev"})
public class DemoUserLoginConfig {

	private static final Logger LOG = LoggerFactory.getLogger(DemoUserLoginConfig.class);

	@Autowired
	private AuthorizationProperties _properties;
	@Autowired
	private UserService _userInformationService;
	@Autowired
	private ApiInfo _apiInfo;

	@Bean
	@Order(-1)
	public WebSecurityPlugin addDemoLogins() {
		LOG.info("Getting realm name from {}", _apiInfo);
		final String apiTitle = _apiInfo.getTitle();

		return http -> {
			LOG.info("Configuring form login and basic auth on {} with realm {}.", http, apiTitle);
			http
				.formLogin()
					.defaultSuccessUrl(UserInformationApiController.USER_INFO_ENDPOINT)
					.and()
				.httpBasic()
					.realmName(apiTitle)
			;
		};
	}

	@Bean
	public UserDetailsService getUserService() {
		LOG.info("Configuring demo users from {}.", _properties);
		List<UserDetails> users = _properties.getGrants().getOrDefault(AuthenticationType.TEST, Collections.emptyList())
				.stream()
				.map(u -> {
					_userInformationService.createUserOrUpdateLastSeen(u.getName(), u.getDescription());

					return User
					.withUsername(u.getName())
					.password("{noop}"+ u.getName())
					.authorities(u.getAuthorities())
					.build();
				})
				.collect(Collectors.toList()
		);

		return new InMemoryUserDetailsManager(users);
	}
}
