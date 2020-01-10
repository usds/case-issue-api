package gov.usds.case_issues.db;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * A mix-in class to add Hibernate support for "functions" that are actually wrappers around
 * postgresql's non-standard operators for JSON fields (e.g. <code>@></code>).
 */
public class JsonOperatorContributor implements MetadataBuilderContributor {

	/** Wrapper function for <code>@></code>: returns true if the first argument contains the second argument. */
	public static final String JSON_CONTAINS = "json_contains";

	@Override
	public void contribute(MetadataBuilder metadataBuilder) {
		metadataBuilder.applySqlFunction(JSON_CONTAINS,
			new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN, "(?1 @> ?2::jsonb)"));
	}
}
