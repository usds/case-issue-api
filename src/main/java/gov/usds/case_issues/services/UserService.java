package gov.usds.case_issues.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.OAuthUser;
import gov.usds.case_issues.db.repositories.UserRepository;

@Service
@Transactional
public class UserService {

	@Autowired
	private UserRepository _userRepo;

	public void createUserOrUpdateLastSeen(String id, String printName) {
        OAuthUser user = _userRepo.findByUserId(id);
        if (user != null) {
            user.updateLastSeen();
            _userRepo.save(user);
            return;
        }
        OAuthUser newUser = new OAuthUser(id, printName);
         _userRepo.save(newUser);
	}

}
