package gov.usds.case_issues.db.repositories;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import gov.usds.case_issues.db.model.UserInformation;;


@RepositoryRestResource(
	path="users",
	itemResourceRel="user",
	collectionResourceRel="users",
	collectionResourceDescription=@Description("All users that have logged in at least once.")
)
public interface UserInformationRepository extends CrudRepository<UserInformation, Long> {

	public static final String USER_ID_CACHE = "byUserId";

	@Override
	@CachePut(value=USER_ID_CACHE, key="#entity.getId()")
	@RestResource(exported=false)
	<S extends UserInformation> S save(S entity);

	@Override
	@CacheEvict(value=USER_ID_CACHE, key="#entity.getId()")
	@RestResource(exported=false)
	void delete(UserInformation entity);

	@Cacheable(value=USER_ID_CACHE, key="#userId")
	@RestResource(exported=false)
	public UserInformation findByUserId(String userId);
}
