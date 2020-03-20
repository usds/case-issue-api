package gov.usds.case_issues.config;

import static org.junit.Assert.assertEquals;

import java.security.Principal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class X509MappingConfigTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Test
	public void extractCN_singleRdnName_cnFound() {
		assertEquals("alice", X509MappingConfig.extractCN("CN=alice, O=No Such Agency, L=Washington, ST=DC, C=US"));
	}

	@Test
	public void extractCN_multiRdnName_cnFound() {
		assertEquals("tony", X509MappingConfig.extractCN("CN=tony + UID=f37d63f81c219e90636b55a181b69855, O=Fake CA, L=Washington, ST=DC, C=US"));
	}

	@Test
	public void extractCN_noCn_exception() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Found no CN attribute");
		X509MappingConfig.extractCN("C=NOPERS, O=Nope");
	}

	@Test
	public void extractCN_invalidName_exception() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Unable to parse");
		X509MappingConfig.extractCN("C=NOPERS, 'O=Nope, CN=Fred");
	}

	@Test
	public void getPrintName_noCommonNameMethod_extractedValueFound() {
		Principal p = new Principal() {
			@Override
			public String getName() {
				return "C=RU,O=GRU,CN=Boris";
			}
		};
		assertEquals("Boris", new X509MappingConfig.CommonNameExtractingPrincipal(p).getPrintName());
	}

	@Test
	public void getPrintName_getCommonNameExists_correctValueFound() {
		Principal p = new CommonNameExportingPrincipal();
		assertEquals("Natasha", new X509MappingConfig.CommonNameExtractingPrincipal(p).getPrintName());
	}

	/** A class that has the additional method that CommonNameExtractingPrincipal looks for. */
	public class CommonNameExportingPrincipal implements Principal {

		@Override
		public String getName() {
			return "C=UN,O=SHIELD,CN=Romanov";
		}

		public String getCommonName() {
			return "Natasha";
		}
	}
}
