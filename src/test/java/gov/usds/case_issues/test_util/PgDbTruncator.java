package gov.usds.case_issues.test_util;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("db-postgresql")
@Primary
public class PgDbTruncator implements DbTruncator {

	@Value("${spring.jpa.properties.hibernate.default_schema:public}")
	private String hibernateSchema;

	/** Credit: https://stackoverflow.com/questions/2829158/truncating-all-tables-in-a-postgres-database */
	private static final String TRUNCATE_FUNCTION_TEMPLATE = "DO " +
			"$func$ " +
			"BEGIN " +
			"   EXECUTE " +
			"   (SELECT 'TRUNCATE TABLE ' || string_agg(oid::regclass::text, ', ') || ' CASCADE' " +
			"    FROM   pg_class " +
			"    WHERE  relkind = 'r' " + // only tables
			"    AND relname not like 'databasechangelog%%' " + // no liquibase tables!
			"    AND    relnamespace = '%1$s'::regnamespace " +
			"   ); " +
			"END " +
			"$func$;";

	private static final Logger LOG = LoggerFactory.getLogger(PgDbTruncator.class);

	@Autowired
	private JdbcTemplate jdbc;

	/* (non-Javadoc)
	 * @see gov.usds.case_issues.test_util.DbTruncator#truncateAll()
	 */
	@Override
	@Transactional
	public void truncateAll() {
		jdbc.execute(String.format(TRUNCATE_FUNCTION_TEMPLATE, hibernateSchema));
	}
}
