package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.UserInformation;;


public interface UserRepository extends CrudRepository<UserInformation, Long> {

	public UserInformation findByUserId(String userId);
}
