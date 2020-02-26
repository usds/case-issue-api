package gov.usds.case_issues.config;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;

import gov.usds.case_issues.services.UserService;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty("server.ssl.client-auth") // could be @ConditionalOnExpression and test for "want" or "need"
public class X509MappingConfig {

	private static final Logger LOG = LoggerFactory.getLogger(X509MappingConfig.class);

	@Autowired
	private UserService _userService;

	/**
	 * Return a configuration plugin that sets up x509 (two-way SSL) authentication.
	 */
	@Bean
	public WebSecurityPlugin getX509Configurer() {
		LOG.info("Configuring x509 authentication");
		return http -> {
			AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> detailsService = token -> {
				LOG.debug("Mapping x509 user details for {}", token);
				String printName;
				String userName = token.getName();
				if (token.getPrincipal() instanceof CommonNameExtractingPrincipal) {
					CommonNameExtractingPrincipal principal = (CommonNameExtractingPrincipal) token.getPrincipal();
					LOG.debug("CN is {}", principal.getPrintName());
					printName = principal.getPrintName();
				} else {
					LOG.debug("Falling back to using DN as print name");
					printName = userName;
				}
				_userService.createUserOrUpdateLastSeen(userName, printName);
				return new User(userName, "", Collections.emptyList());
			};

			X509PrincipalExtractor principalExtractor = cert -> {
				Principal p = cert.getSubjectDN();
				LOG.debug("Cert and SubjectDN classes are {} and {}", cert.getClass(), p.getClass());
				return new CommonNameExtractingPrincipal(p);
			};
			http.x509()
				.authenticationUserDetailsService(detailsService)
				.x509PrincipalExtractor(principalExtractor)
				;
		};
	}

	private static class CommonNameExtractingPrincipal implements Principal {

		private Principal wrapped;

		public CommonNameExtractingPrincipal(Principal w) {
			wrapped = w;
		}

		/** Get the Common Name of this user, either by calling the {@link sun.security.x509.X500Name#getCommonName()}
		 * method on the wrapped Principal (if it is an instance of that class) or by parsing the name using
		 * {@link javax.naming.ldap.LdapName}.
		 */
		public String getPrintName() {
			try {
				Method m = wrapped.getClass().getMethod("getCommonName");
				return m.invoke(wrapped).toString();
			} catch (SecurityException | ReflectiveOperationException | IllegalArgumentException  e) {
				return extractCN(wrapped.getName());
			}
		}

		@Override
		public String getName() {
			return wrapped.getName();
		}

		public String toString() {
			return wrapped.toString();
		}
	}

	/** Parse a Distinguished Name and extract the Common Name attribute from it. */
	public static String extractCN(String distinguishedName) {
		LOG.debug("Extracting common name from {}", distinguishedName);
		try {
			LdapName subject = new LdapName(distinguishedName);
			for (Rdn r : subject.getRdns()) {
				NamingEnumeration<? extends Attribute> all = r.toAttributes().getAll();
				while (all.hasMore()) {
					Attribute a = all.next();
					Object attr = a.get();
					LOG.debug("Next attribute: {}={}", a.getID(), attr);
					if ("CN".equalsIgnoreCase(a.getID())) {
						return attr.toString();
					}
				}
			}
		} catch (NamingException e) {
			throw new IllegalArgumentException("Unable to parse subject name", e);
		}
		throw new IllegalArgumentException("Found no CN attribute");
	}
}
