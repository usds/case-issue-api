package gov.usds.case_issues.config;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import gov.usds.case_issues.authorization.CaseIssuePermission;
import gov.usds.case_issues.authorization.CustomAccessDeniedHandler;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=false, prePostEnabled=true)
@Configuration
@ConditionalOnWebApplication
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired(required=false)
	private List<WebSecurityPlugin> _configPlugins = Collections.emptyList();

	@Value("${spring.data.rest.basePath}")
	private String _resourceApiBase;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOGGER.info("Configuring HTTP Security on {}", http);
		// these could be plugins, but they work already
		configureResourceUrls(http);
		configureSwaggerUi(http);
		for (WebSecurityPlugin p : _configPlugins) {
			p.apply(http);
		}
		http
			.cors()
				.and()
			.headers()
				.httpStrictTransportSecurity().and()
				.and()
			.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/health", "/favicon.ico")
					.permitAll()
				.requestMatchers(EndpointRequest.to(InfoEndpoint.class))
					.permitAll()
				.requestMatchers(EndpointRequest.toAnyEndpoint())
					.hasAuthority(CaseIssuePermission.MANAGE_APPLICATION.name())
				.anyRequest()
					.authenticated()
				.and()
			.csrf()
				.and()
			.exceptionHandling()
				.accessDeniedHandler(new CustomAccessDeniedHandler())
		;
	}

	private void configureResourceUrls(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.mvcMatchers(HttpMethod.GET, _resourceApiBase)
				.permitAll()
			.antMatchers(HttpMethod.GET, _resourceApiBase + "/explorer/**")
				.permitAll()
			.antMatchers(_resourceApiBase + "/**")
				.hasAuthority(CaseIssuePermission.UPDATE_STRUCTURE.name())
		;
	}

	private void configureSwaggerUi(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers(HttpMethod.GET,
					"/",
					"/csrf",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v2/api-docs",
					"/swagger-resources/**")
				.permitAll()
		;
	}
}
