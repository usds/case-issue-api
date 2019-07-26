package gov.usds.case_issues.config;

import java.util.List;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonView;

import gov.usds.case_issues.model.ApiViews;

/**
 * Configuration class for customizing JSON serialization.
 */
@Configuration
public class JacksonConfig {

	@Bean public Jackson2ObjectMapperBuilderCustomizer foo() {
		return new Jackson2ObjectMapperBuilderCustomizer() {

			@Override
			public void customize(Jackson2ObjectMapperBuilder builder) {
				builder.mixIn(Page.class, JsonPage.class);
			}
		};
	}

	/** Marker interface to show which fields of the {@link Page} object we want to expose in JSON. */
	public interface JsonPage<T> extends Page<T> {

		@JsonView(ApiViews.All.class)
		int getNumber();

		@JsonView(ApiViews.All.class)
		int getSize();

		@JsonView(ApiViews.All.class)
		int getNumberOfElements();

		@JsonView(ApiViews.All.class)
		List<T> getContent();

		@JsonView(ApiViews.All.class)
		boolean hasContent();

		@JsonView(ApiViews.All.class)
		Sort getSort();

		@JsonView(ApiViews.All.class)
		boolean isFirst();

		@JsonView(ApiViews.All.class)
		boolean isLast();

		@JsonView(ApiViews.All.class)
		int getTotalPages();

		@JsonView(ApiViews.All.class)
		long getTotalElements();
		
	}
}
