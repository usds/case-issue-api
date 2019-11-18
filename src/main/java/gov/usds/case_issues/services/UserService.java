package gov.usds.case_issues.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserRepository;
import gov.usds.case_issues.model.SerializedUserInformation;

@Service
@Transactional(readOnly = false)
public class UserService {

	@Autowired
	private UserRepository _userRepo;

	public SerializedUserInformation getCurrentUser(Authentication auth) {
		String id = auth.getName();
		UserInformation user = _userRepo.findByUserId(id);
		return new SerializedUserInformation(user.getId(), user.getPrintName());
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
