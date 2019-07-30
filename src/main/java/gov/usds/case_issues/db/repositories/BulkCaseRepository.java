package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.rest.core.annotation.RestResource;

import gov.usds.case_issues.db.model.TroubleCase;

@RepositoryDefinition(idClass=Long.class, domainClass=TroubleCase.class)
@RestResource(exported=false)
public interface BulkCaseRepository {

	@Query(name="snoozed")
	public List<Object[]> getSnoozedCases(Long caseManagementSystemId, Long caseTypeId, Pageable p);

	@Query(name="unSnoozed")
	public List<Object[]> getActiveCases(Long caseManagementSystemId, Long caseTypeId, Pageable p);

	@Query(name="summary")
	public List<Object[]> getSnoozeSummary(Long caseManagementSystemId, Long caseTypeId);
}
