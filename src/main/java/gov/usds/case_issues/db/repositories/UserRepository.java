package gov.usds.case_issues.db.repositories;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.UserInformation;;


public interface UserRepository extends CrudRepository<UserInformation, Long> {

	@Override
	@CacheEvict("byUserId")
	<S extends UserInformation> S save(S entity);

	@Override
	@CacheEvict("byUserId")
	void delete(UserInformation entity);

	@Cacheable(value="byUserId", key="#userId")
	public UserInformation findByUserId(String userId);
}
