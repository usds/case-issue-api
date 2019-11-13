package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.OAuthUser;;


public interface UserRepository extends CrudRepository<OAuthUser, Long> {

	public OAuthUser findByUserId(String userId);
}
