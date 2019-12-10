package gov.usds.case_issues.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserInformationRepository;
import gov.usds.case_issues.model.SerializedUserInformation;

@Service
@Transactional(readOnly = false)
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
		return new SerializedUserInformation(user.getId(), user.getPrintName());
	}

	public void createUserOrUpdateLastSeen(String id, String printName) {
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

	/**
	 * Clear the cache, forcing re-fetch of UserInformation records from the database.
	 * <b>Should only be needed for tests!</b>
	 */
	@CacheEvict(cacheNames=UserInformationRepository.USER_ID_CACHE, allEntries=true)
	public void clearCache() {
		LOG.warn("Manually emptying UserInformation cache");
	}
}
