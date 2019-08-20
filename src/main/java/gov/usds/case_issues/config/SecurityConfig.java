package gov.usds.case_issues.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import springfox.documentation.service.ApiInfo;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private WebConfigurationProperties _webProperties;
	@Autowired
	private ApiInfo _apiInfo;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOGGER.info("Configuring HTTP Security");
		http
			.cors()
				.and()
			.formLogin()
				.defaultSuccessUrl("/user")
				.and()
			.httpBasic()
				.realmName(_apiInfo.getTitle())
				.and()
			.authorizeRequests()
				.antMatchers("/actuator/health", "/health")
					.permitAll()
				.anyRequest()
					.authenticated()
				.and()
			.csrf()
				.ignoringRequestMatchers(AnyRequestMatcher.INSTANCE)
		;
	}

	@Bean
	@Profile("dev")
	public UserDetailsService getUserService() {
		List<UserDetails> users = _webProperties.getUsers().stream()
			.map(u -> User.withUsername(u.getName())
						.password("{noop}"+ u.getName())
						.authorities(u.getGrants().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
						.build())
			.collect(Collectors.toList()
		);
		return new InMemoryUserDetailsManager(users);
	}
}
