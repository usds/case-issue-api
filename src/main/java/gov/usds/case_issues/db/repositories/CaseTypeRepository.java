package gov.usds.case_issues.db.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.CaseType;

/**
 * Repository for case types (e.g. forms).
 */
public interface CaseTypeRepository extends CrudRepository<CaseType, Long> {

	public Optional<CaseType> findByCaseTypeTag(String tag);

}
