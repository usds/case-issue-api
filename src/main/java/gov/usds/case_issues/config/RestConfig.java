package gov.usds.case_issues.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TaggedEntity;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.NoteSubtypeRepository;
import gov.usds.case_issues.db.repositories.TaggedEntityRepository;

@Configuration
@ConditionalOnWebApplication
public class RestConfig implements RepositoryRestConfigurer {

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.withEntityLookup()
			.forRepository(CaseManagementSystemRepository.class)
				.withIdMapping(CaseManagementSystem::getExternalId)
				.withLookup(CaseManagementSystemRepository::findByExternalId)
			.forRepository(CaseTypeRepository.class)
				.withIdMapping(CaseType::getExternalId)
				.withLookup(CaseTypeRepository::findByExternalId)
			.forRepository(NoteSubtypeRepository.class)
				.withIdMapping(TaggedEntity::getExternalId)
				.withLookup(TaggedEntityRepository::findByExternalId)
		;
	}
}
