package gov.usds.case_issues.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration that customizes Spring MVC for our specific needs.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new InputStreamMessageConverter());
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
