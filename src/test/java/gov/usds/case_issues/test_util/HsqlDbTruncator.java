package gov.usds.case_issues.test_util;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HsqlDbTruncator {

	private static final Logger LOG = LoggerFactory.getLogger(HsqlDbTruncator.class);

	@Autowired
	private JdbcTemplate jdbc;

	@Transactional
	public void truncateAll() {
		LOG.warn("Attempting to truncate all tables.");
		jdbc.execute("TRUNCATE SCHEMA PUBLIC AND COMMIT NO CHECK");
	}
}
