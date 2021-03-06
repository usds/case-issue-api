package gov.usds.case_issues.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserInformationRepository;
import gov.usds.case_issues.model.SerializedUserInformation;
import gov.usds.case_issues.validators.PersistedId;

@Service
@Transactional(readOnly = false)
@Validated
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserInformationRepository _userRepo;

	@Transactional(readOnly=true)
	public SerializedUserInformation getCurrentUser(Authentication auth) {
		String id = auth.getName();
		UserInformation user = _userRepo.findByUserId(id);
		if (user == null) {
			throw new IllegalArgumentException("User information is not populated in the database");
		}
		return new SerializedUserInformation(user);
	}

	public void createUserOrUpdateLastSeen(@PersistedId String id, @PersistedId String printName) {
		UserInformation user = _userRepo.findByUserId(id);
		if (user != null) {
			user.updateLastSeen();
			String foundName = user.getPrintName();
			if (foundName == null || !foundName.equals(printName)) {
				LOG.info("Updated print name for {}", user.getId());
				user.setPrintName(printName);
			}
		} else {
			user = new UserInformation(id, printName);
		}
		_userRepo.save(user);
	}
}
