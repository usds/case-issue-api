package gov.usds.case_issues.test_util;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class HsqlDbTruncator implements DbTruncator {

	private static final String TABLE_QUERY = "SELECT table_name "
			+ "from INFORMATION_SCHEMA.SYSTEM_TABLES "
			+ "where TABLE_SCHEM=? and table_type='TABLE' "
			+ "and table_name not like 'DATABASECHANGELOG%'";

	private static final Logger LOG = LoggerFactory.getLogger(HsqlDbTruncator.class);

	private List<String> allTables;
	@Value("${spring.jpa.properties.hibernate.default_schema:public}")
	private String hibernateSchema;

	@Autowired
	private JdbcTemplate jdbc;

	@Transactional
	public void truncateAll() {
		LOG.warn("Attempting to truncate all tables.");
		String sql = TABLE_QUERY;
		if (allTables == null) {
			allTables = jdbc.queryForList(sql, hibernateSchema.toUpperCase()).stream()
					.map(m->(String) m.get("TABLE_NAME"))
					.collect(Collectors.toList());
			LOG.info("Initialized HSQLDB table list: {}", allTables);
		}

		for (String tableName : allTables) {
			jdbc.execute("TRUNCATE TABLE " + tableName + " AND COMMIT NO CHECK");
		}
	}
}
