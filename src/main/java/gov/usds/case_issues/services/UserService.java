package gov.usds.case_issues.services;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserRepository;

@Service
@Transactional
public class UserService {

	@Autowired
	private UserRepository _userRepo;

	public HashMap<String, String> getCurrentUser(Authentication auth) {
		String id = auth.getName();
		UserInformation user = _userRepo.findByUserId(id);
		HashMap<String, String> response = new HashMap<String, String>();
		response.put("ID", user.getId());
		response.put("name", user.getPrintName());
		return response;
	}

	public void createUserOrUpdateLastSeen(String id, String printName) {
		UserInformation user = _userRepo.findByUserId(id);
		if (user != null) {
			user.updateLastSeen();
			_userRepo.save(user);
			return;
		}
		UserInformation newUser = new UserInformation(id, printName);
		_userRepo.save(newUser);
	}

}
