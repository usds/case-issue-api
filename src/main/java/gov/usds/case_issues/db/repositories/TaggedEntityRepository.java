package gov.usds.case_issues.db.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import gov.usds.case_issues.db.model.TaggedEntity;

@NoRepositoryBean
public interface TaggedEntityRepository<T extends TaggedEntity> extends CrudRepository<T, Long> {

	/** Find the system based on the URL-safe tag (e.g. "CM1K") */
	public Optional<T> findByExternalId(String tag);
	/** Find the system based on its proper name (e.g. "Case Manager 1000") */
	public Optional<T> findByName(String name);

}
