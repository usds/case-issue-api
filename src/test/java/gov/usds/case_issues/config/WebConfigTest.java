package gov.usds.case_issues.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;

import org.apache.catalina.connector.Connector;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

import gov.usds.case_issues.test_util.MockConfig;

public class WebConfigTest {

	private WebConfigurationProperties _wrappedProperties = new MockConfig().getMockProperties();
	private WebConfig _config = new WebConfig(_wrappedProperties);

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void getServerCustomizer_validPort_ok() {
		int validPort = 123;
		Mockito.when(_wrappedProperties.getAdditionalHttpPort()).thenReturn(validPort);
		WebServerFactoryCustomizer<TomcatServletWebServerFactory> c = _config.getServerCustomizer();
		TomcatServletWebServerFactory factory = Mockito.mock(TomcatServletWebServerFactory.class);
		c.customize(factory);
		ArgumentCaptor<Connector> arg = ArgumentCaptor.forClass(Connector.class);
		Mockito.verify(factory, atLeastOnce()).addAdditionalTomcatConnectors(arg.capture());
		assertEquals(validPort, arg.getValue().getPort());
	}

	@Test(expected=IllegalArgumentException.class)
	public void getServerCustomizer_zeroPort_exception() {
		Mockito.when(_wrappedProperties.getAdditionalHttpPort()).thenReturn(0);
		_config.getServerCustomizer();
	}

	@Test(expected=IllegalArgumentException.class)
	public void getServerCustomizer_negativePort_exception() {
		Mockito.when(_wrappedProperties.getAdditionalHttpPort()).thenReturn(-1);
		_config.getServerCustomizer();
	}

}
