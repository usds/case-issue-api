package gov.usds.case_issues.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TaggedEntity;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.AttachmentSubtypeRepository;
import gov.usds.case_issues.db.repositories.TaggedEntityRepository;

@Configuration
@ConditionalOnWebApplication
public class RestConfig implements RepositoryRestConfigurer {

	private static final Logger LOG = LoggerFactory.getLogger(RestConfig.class);

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.withEntityLookup()
			.forRepository(CaseManagementSystemRepository.class)
				.withIdMapping(CaseManagementSystem::getExternalId)
				.withLookup(CaseManagementSystemRepository::findByExternalId)
			.forRepository(CaseTypeRepository.class)
				.withIdMapping(CaseType::getExternalId)
				.withLookup(CaseTypeRepository::findByExternalId)
			.forRepository(AttachmentSubtypeRepository.class)
				.withIdMapping(TaggedEntity::getExternalId)
				.withLookup(TaggedEntityRepository::findByExternalId)
		;
		LOG.info("Disabling CORS access to repository REST resources");

		// this is very broad, but only applies when controllers managed by this configuration, so
		// we can leave it broad instead of tailoring it to our actual URL configuration.
		config.getCorsRegistry().addMapping("/**")
			.allowedOrigins(new String[0]);
	}
}
