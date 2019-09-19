package gov.usds.case_issues.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration that customizes Spring MVC for our specific needs.
 */
@Configuration
@ConditionalOnWebApplication
public class WebConfig implements WebMvcConfigurer {

	private static final Logger LOG = LoggerFactory.getLogger(WebConfig.class);

	/** A filter order that allows us to get in before the Spring Security filter chain. */
	private static final int BEFORE_SECURITY = -100;

	@Autowired
	private WebConfigurationProperties _customProperties;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] origins = _customProperties.getCorsOrigins();
		// NOTE: Spring Data Rest (/resources) CORS configuration is in RestConfig, not here.
		LOG.info("Configuring CORS allowed origins for API to {}", Arrays.toString(origins));
		if (origins != null && 0 < origins.length) {
			registry.addMapping("/api/**")
					.allowCredentials(true)
					.allowedMethods("*")
					.allowedOrigins(origins);
			registry.addMapping("/user")
					.allowCredentials(true)
					.allowedMethods("GET")
					.allowedOrigins(origins);
			registry.addMapping("/csrf")
					.allowCredentials(true)
					.allowedMethods("GET")
					.allowedOrigins(origins);
		}
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addStatusController("/health", HttpStatus.OK);
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new InputStreamMessageConverter());
	}

	@Bean
	public FilterRegistrationBean<ForwardedHeaderFilter> getForwardFilter() {
		ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
		// some reverse-proxy setups allegedly have problems with this, but until somebody complains, going with
		// the IETF recommendation over the servlet spec: https://tools.ietf.org/html/rfc7231#section-7.1.2
		filter.setRelativeRedirects(true);
		FilterRegistrationBean<ForwardedHeaderFilter> reg = new FilterRegistrationBean<ForwardedHeaderFilter>(filter);
		reg.setOrder(OrderedFilter.HIGHEST_PRECEDENCE);
		return reg;
	}

	@Bean
	public FilterRegistrationBean<JsonRedirectPreventingFilter> getRedirectPreventingFilter() {
		FilterRegistrationBean<JsonRedirectPreventingFilter> registration = new FilterRegistrationBean<>(new JsonRedirectPreventingFilter());
		registration.setOrder(BEFORE_SECURITY);
		return registration;
	}

	/**
	 * Trivial {@link HttpMessageConverter} implementation to allow handler methods to accept
	 * "text/csv" input as a raw input stream.
	 */
	private static class InputStreamMessageConverter extends AbstractHttpMessageConverter<InputStream> {

		public InputStreamMessageConverter() {
			super(new MediaType("text", "csv"));
		}

		@Override
		protected boolean supports(Class<?> clazz) {
			return clazz.equals(InputStream.class);
		}

		@Override
		protected InputStream readInternal(Class<? extends InputStream> clazz, HttpInputMessage inputMessage)
				throws IOException, HttpMessageNotReadableException {
			return inputMessage.getBody();
		}

		@Override
		protected void writeInternal(InputStream t, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {
			throw new IllegalArgumentException("This HttpMessageConverter does not support output.");
		}

		@Override
		public boolean canWrite(Class<?> clazz, MediaType mediaType) {
			return false; // this is an input-only converter.
		}
	}
}
