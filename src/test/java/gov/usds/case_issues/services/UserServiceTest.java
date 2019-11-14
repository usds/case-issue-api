package gov.usds.case_issues.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.OAuthUser;
import gov.usds.case_issues.db.repositories.UserRepository;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class UserServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private UserRepository _userRepo;
	@Autowired
	private UserService _service;

	@Before
	public void clear() {
		truncateDb();
	}

	@Test
	public void createUserOrUpdateLastSeen_newUser_userCreated() {
		_service.createUserOrUpdateLastSeen("cbc1b10b-fe73-4367-806f-027d30d27f84", "Tim");
		assertNotNull(_userRepo.findByUserId("cbc1b10b-fe73-4367-806f-027d30d27f84"));
	}

	@Test
	public void createUserOrUpdateLastSeen_existingUser_lastSeenUpdated() {
		ZonedDateTime now = ZonedDateTime.now();
		_userRepo.save(new OAuthUser("cbc1b10b-fe73-4367-806f-027d30d27f84", "Tim"));
		_service.createUserOrUpdateLastSeen("cbc1b10b-fe73-4367-806f-027d30d27f84", "Tim");
		OAuthUser user = _userRepo.findByUserId("cbc1b10b-fe73-4367-806f-027d30d27f84");
		assertTrue(user.getLastSeen().compareTo(now) > 0);
	}
}
