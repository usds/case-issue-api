package gov.usds.case_issues.test_util;

import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HsqlDbTruncator implements DbTruncator {

	private static final Logger LOG = LoggerFactory.getLogger(HsqlDbTruncator.class);

	private final List<String> allTables = Arrays.asList(
		"case_management_system",
		"case_type",
		"trouble_case",
		"case_issue",
		"case_snooze",
		"note_subtype",
		"case_note",
		"note_association"
	);

	@Autowired
	private JdbcTemplate jdbc;

	@Transactional
	public void truncateAll() {
		LOG.warn("Attempting to truncate all tables.");
		for (String tableName : allTables) {
			jdbc.execute("TRUNCATE TABLE " + tableName + " AND COMMIT NO CHECK");
		}
	}
}
