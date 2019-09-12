package gov.usds.case_issues.config;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter;
import org.springframework.security.web.csrf.CsrfToken;

import springfox.documentation.service.ApiInfo;

@Configuration
@Profile({"dev"})
public class DemoUserLoginConfig {

	private static final Logger LOG = LoggerFactory.getLogger(DemoUserLoginConfig.class);

	@Autowired
	private WebConfigurationProperties _webProperties;
	@Autowired
	private ApiInfo _apiInfo;

	@Bean
	@Order(-1)
	public WebSecurityPlugin addDemoLogins() {
		final String apiTitle = _apiInfo.getTitle();

		return http -> {
			LOG.info("Configuring form login and basic auth on {} with realm {}.", http, apiTitle);
			http
				.formLogin()
					// .loginPage("/login")
					.defaultSuccessUrl("/user")
					.and()
				.httpBasic()
					.realmName(apiTitle)
					.and()
				.authorizeRequests()
					.antMatchers("/login*")
					.permitAll();

			Function<HttpServletRequest, Map<String, String>> hiddenInputs = request -> {
				CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
				if (token == null) {
					return Collections.emptyMap();
				}
				return Collections.singletonMap(token.getParameterName(), token.getToken());
			};
			DefaultLoginPageGeneratingFilter loginPageGeneratingFilter = new DefaultLoginPageGeneratingFilter();
			DefaultLogoutPageGeneratingFilter logoutPageGeneratingFilter = new DefaultLogoutPageGeneratingFilter();
			loginPageGeneratingFilter.setResolveHiddenInputs(hiddenInputs);
			logoutPageGeneratingFilter.setResolveHiddenInputs(hiddenInputs);
			http.setSharedObject(DefaultLoginPageGeneratingFilter.class, loginPageGeneratingFilter);
			// loginPageGeneratingFilter = postProcess(loginPageGeneratingFilter);

			http
				.addFilter(loginPageGeneratingFilter)
				.addFilter(logoutPageGeneratingFilter);

			;
		};
	}

	@Bean
	public UserDetailsService getUserService() {
		LOG.info("Configuring demo users from {}.", _webProperties);
		List<UserDetails> users = _webProperties.getUsers().stream()
				.map(u -> User
					.withUsername(u.getName())
					.password("{noop}"+ u.getName())
					.authorities(u.getGrants())
					.build())
				.collect(Collectors.toList()
		);
		return new InMemoryUserDetailsManager(users);
	}
}
