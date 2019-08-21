package gov.usds.case_issues.test_util;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("autotest")
@WithAnonymousUser
public abstract class CaseIssueApiTestBase {

	@Autowired
	private HsqlDbTruncator _truncator;
	@Autowired
	protected FixtureDataInitializationService _dataService;

	protected void truncateDb() {
		_truncator.truncateAll();
	}
}
