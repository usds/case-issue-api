package gov.usds.case_issues.test_util;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("db-postgresql")
@Primary
public class PgDbTruncator implements DbTruncator {

	/** Credit: https://stackoverflow.com/questions/2829158/truncating-all-tables-in-a-postgres-database */
	private static final String TRUNCATE_FUNCTION = "DO " +
			"$func$ " +
			"BEGIN " +
			"   EXECUTE " +
			"   (SELECT 'TRUNCATE TABLE ' || string_agg(oid::regclass::text, ', ') || ' CASCADE' " +
			"    FROM   pg_class " +
			"    WHERE  relkind = 'r' " + // only tables
			"    AND oid::regclass::text not like 'databasechangelog%' " + // no liquibase tables!
			"    AND    relnamespace = 'public'::regnamespace " + // probably should be property-driven
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
		LOG.warn("Attempting to truncate all tables.");
		jdbc.execute(TRUNCATE_FUNCTION);
	}
}
