package gov.usds.case_issues.test_util;

import javax.transaction.Transactional;

public interface DbTruncator {

	void truncateAll();

}