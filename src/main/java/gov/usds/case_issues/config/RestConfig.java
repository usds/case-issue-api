package gov.usds.case_issues.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.withEntityLookup()
			.forRepository(CaseManagementSystemRepository.class)
				.withIdMapping(CaseManagementSystem::getCaseManagementSystemTag)
				.withLookup(CaseManagementSystemRepository::findByCaseManagementSystemTag)
			.forRepository(CaseTypeRepository.class)
				.withIdMapping(CaseType::getCaseTypeTag)
				.withLookup(CaseTypeRepository::findByCaseTypeTag)
		;
	}
}
