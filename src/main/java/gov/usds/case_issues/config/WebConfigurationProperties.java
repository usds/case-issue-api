package gov.usds.case_issues.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
@ConfigurationProperties(prefix="web-customization", ignoreUnknownFields=false)
public class WebConfigurationProperties {

	private String[] _corsOrigins;
	private Map<String, DataFormatSpec> _dataFormats = new HashMap<>();
	private int additionalHttpPort;

	public void setCorsOrigins(String[] origins) {
		_corsOrigins = origins;
	}

	public String[] getCorsOrigins() {
		return _corsOrigins;
	}

	public Map<String, DataFormatSpec> getDataFormats() {
		return _dataFormats;
	}

	public void setDataFormats(Map<String, DataFormatSpec> dataFormats) {
		this._dataFormats = dataFormats;
	}

	public int getAdditionalHttpPort() {
		return additionalHttpPort;
	}

	public void setAdditionalHttpPort(int additionalHttpPort) {
		this.additionalHttpPort = additionalHttpPort;
	}
}
