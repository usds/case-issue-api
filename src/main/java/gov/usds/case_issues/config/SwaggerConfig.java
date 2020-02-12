package gov.usds.case_issues.config;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.info.BuildProperties;
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
			@Value("${api.contact.name:#{null}}") Optional<String> contactName,
			@Value("${api.contact.url:#{null}}") Optional<String> contactUrl,
			@Value("${api.contact.email:#{null}}") Optional<String> contactEmail,
			@Autowired Optional<BuildProperties> buildProps
			) {
		if (contactName.isPresent() && !(contactUrl.isPresent() || contactEmail.isPresent())) {
			throw new IllegalArgumentException(
				"A contact email or url is required when providing an API contact name."
			);
		}
		String apiVersion = buildProps.isPresent() ? buildProps.get().getVersion() : "DEVELOPMENT";
		Contact contact = new Contact(contactName.orElse(null), contactUrl.orElse(null), contactEmail.orElse(null));
		ApiInfo apiInfo = new ApiInfo(
				apiTitle, apiDescription, apiVersion, null, contact,
				null, null, Collections.emptyList());
		LOGGER.info("Created ApiInfo for {}", apiTitle);
		return apiInfo;
	}

	@Bean
	public Docket configureSwagger(@Autowired ApiInfo apiInfo,
			@Value("${api.base.package:#{null}}") Optional<String> basePackageOptional) {
		String basePackage = basePackageOptional.orElseGet(() -> {
			String guess = this.getClass().getPackage().getName().replaceAll("\\.\\w+\\z", "");
			LOGGER.info("Guessing API base package as {}", guess);
			return guess;
		});
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
