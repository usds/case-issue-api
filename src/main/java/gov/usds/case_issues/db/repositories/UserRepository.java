package gov.usds.case_issues.db.repositories;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.User;


public interface UserRepository extends CrudRepository<User, Long> {

	public User findByUserId(String userId);
}
