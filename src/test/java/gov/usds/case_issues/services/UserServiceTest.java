package gov.usds.case_issues.services;

import static gov.usds.case_issues.test_util.Assert.assertInstantOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserInformationRepository;
import gov.usds.case_issues.model.SerializedUserInformation;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class UserServiceTest extends CaseIssueApiTestBase {

	private static final String SAMPLE_ID = "cbc1b10b-fe73-4367-806f-027d30d27f84";
	@Autowired
	private UserInformationRepository _userRepo;
	@Autowired
	private UserService _service;

	private ZonedDateTime _testStart;

	@Before
	public void clear() {
		truncateDb();
		_testStart = ZonedDateTime.now();
	}

	@Test
	public void createUserOrUpdateLastSeen_newUser_userCreated() {
		String myName = "Tim";
		_service.createUserOrUpdateLastSeen(SAMPLE_ID, myName);
		UserInformation found = _userRepo.findByUserId(SAMPLE_ID);
		assertNotNull(found);
		assertEquals(SAMPLE_ID, found.getId());
		assertEquals(myName, found.getPrintName());
	}

	@Test
	public void createUserOrUpdateLastSeen_existingUser_lastSeenUpdated() {
		assertEquals(0, _userRepo.count());
		Date firstSeen = _userRepo.save(new UserInformation(SAMPLE_ID, "Tim")).getLastSeen();
		_service.createUserOrUpdateLastSeen(SAMPLE_ID, "Tim");
		UserInformation user = _userRepo.findByUserId(SAMPLE_ID);
		assertInstantOrder(_testStart.toInstant(), user.getLastSeen().toInstant(), false);
		assertInstantOrder(firstSeen.toInstant(), user.getLastSeen().toInstant(), false);
	}

	@Test
	public void createUserOrUpdateLastSeen_existingUserNewName_nameUpdated() {
		assertEquals(0, _userRepo.count());
		_service.createUserOrUpdateLastSeen(SAMPLE_ID, "Tim");
		UserInformation found = _userRepo.findByUserId(SAMPLE_ID);
		assertEquals("Tim", found.getPrintName());
		assertEquals(1, _userRepo.count());
		_service.createUserOrUpdateLastSeen(SAMPLE_ID, "Timothy");
		found = _userRepo.findByUserId(SAMPLE_ID);
		assertEquals("Timothy", found.getPrintName());
		assertEquals(1, _userRepo.count());
	}

	@Test(expected=IllegalArgumentException.class)
	public void getCurrentUser_noUser_error() {
		_service.getCurrentUser(new MiniAuth(SAMPLE_ID));
	}

	@Test
	public void getCurrentUser_userExists_expectedResult() {
		String id = "UUUUUID";
		_service.createUserOrUpdateLastSeen(id, "Fred Jones");
		SerializedUserInformation u = _service.getCurrentUser(new MiniAuth(id));
		assertNotNull(u);
		assertEquals("Fred Jones", u.getName());
		assertEquals(id, u.getID());
	}

	@Test(expected=ConstraintViolationException.class)
	public void createUserOrUpdateLastSeen_userNameTooLong_catastrophe() {
		StringBuilder longString = new StringBuilder();
		while (longString.length() < 300) {
			longString.append("abcde");
		}
		_service.createUserOrUpdateLastSeen(longString.toString(), "Irrelevant");
	}

	@Test(expected=ConstraintViolationException.class)
	public void createUserOrUpdateLastSeen_printNameTooLong_catastrophe() {
		StringBuilder longString = new StringBuilder();
		while (longString.length() < 300) {
			longString.append("abcde");
		}
		_service.createUserOrUpdateLastSeen("irrelevant", longString.toString());

	}

	/** Minimal Authentication implementation for tests */
	private static class MiniAuth extends AbstractAuthenticationToken {
		private static final long serialVersionUID = 1L;
		private String _name;

		private MiniAuth(String name) {
			super(Collections.emptyList());
			_name = name;
		}

		@Override
		public Object getCredentials() { return null; }

		@Override
		public Object getPrincipal() {
			return _name;
		}
	}
}
