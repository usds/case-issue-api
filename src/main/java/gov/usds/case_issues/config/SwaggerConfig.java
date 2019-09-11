package gov.usds.case_issues.config;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.classmate.TypeResolver;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
@PropertySource(ignoreResourceNotFound=true,value="api.properties")
@ConditionalOnWebApplication
public class SwaggerConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerConfig.class);

	@Bean
	public ApiInfo createApiInfo(
			@Value("${api.title:API}") String apiTitle,
			@Value("${api.description:#{null}}") String apiDescription,
			@Value("${api.version:1.0.0}") String apiVersion,
			@Value("${api.contact.name:#{null}}") String contactName,
			@Value("${api.contact.url:#{null}}") String contactUrl,
			@Value("${api.contact.email:#{null}}") String contactEmail
			) {
		Contact contact = new Contact(contactName, contactUrl, contactEmail);
		if (contactName != null && contactUrl == null && contactEmail == null) {
			throw new IllegalArgumentException("API contact name is useless if both API contact email and contact URL are null.");
		}
		ApiInfo apiInfo = new ApiInfo(
				apiTitle, apiDescription, apiVersion, null, contact,
				null, null, Collections.emptyList());
		LOGGER.info("Created ApiInfo for {}", apiTitle);
		return apiInfo;
	}

	@Bean
	public Docket configureSwagger(@Autowired ApiInfo apiInfo,
			@Value("${api.base.package:#{null}}") String basePackage) {
		if (basePackage == null) {
			// we assume we're in a "config" or similar subpackage and go up one level for the base of the app
			basePackage = this.getClass().getPackage().getName().replaceAll("\\.\\w+\\z", "");
			LOGGER.info("Guessing API base package as {}", basePackage);
		}
		TypeResolver typeResolver = new TypeResolver();
		return new Docket(DocumentationType.SWAGGER_2)
			.select()
				.apis(RequestHandlerSelectors.basePackage(basePackage))
				.build()
			.alternateTypeRules(
				// List gets translated to JSON Array automatically, so translate Iterable to List
				AlternateTypeRules.newRule(
						typeResolver.resolve(Iterable.class, WildcardType.class),
						typeResolver.resolve(List.class, WildcardType.class)
				),
				AlternateTypeRules.newRule(
						typeResolver.resolve(Timestamp.class),
						typeResolver.resolve(String.class)
				)
			)
			.apiInfo(apiInfo)
		;
	}

	@Bean
	public UiConfiguration configureUi() {
		return UiConfigurationBuilder.builder()
			.displayRequestDuration(true)
			.build();
	}
}
