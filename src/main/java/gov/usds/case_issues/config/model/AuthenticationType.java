package gov.usds.case_issues.config.model;

/**
 * An enumeration of the types of authentication we support,
 * for use in configuration properties.
 */
public enum AuthenticationType {
	/** Test users for local development, using basic auth. */
	TEST,
	/** Externally authenticated users using the OAuth2/OIDC mapper. */
	OAUTH,
	/** Pre-authenticated users presenting an x509 certificate. */
	X509,
}
