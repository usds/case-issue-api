package gov.usds.case_issues.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="web-customization", ignoreUnknownFields=false)
public class WebConfigurationProperties {

	private String[] _corsOrigins;

	public void setCorsOrigins(String[] origins) {
		_corsOrigins = origins;
	}

	public String[] getCorsOrigins() {
		return _corsOrigins;
	}
}
