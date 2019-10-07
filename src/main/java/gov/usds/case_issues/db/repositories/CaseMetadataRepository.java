package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.CaseMetadata;

public interface CaseMetadataRepository extends CrudRepository<CaseMetadata, Long> {

	@Query("SELECT c "
		+  "FROM #{#entityName} c "
		+  "ORDER BY c.lastUpdated DESC "
		)
	List<CaseMetadata> findAllOrderByLastUpdatedDesc();
}
