package gov.usds.case_issues.db.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.reporting.SneakyViewEntity;

public interface SneakyReportRepo extends PagingAndSortingRepository<SneakyViewEntity, Long> {

	Page<SneakyViewEntity> getAllByCaseManagementSystemAndCaseType(CaseManagementSystem caseManager, CaseType caseType, Pageable pageable);

	Page<SneakyViewEntity> getAllByCaseManagementSystemAndCaseTypeAndHasOpenIssue(CaseManagementSystem caseManager, CaseType caseType, boolean hasOpenIssue, Pageable pageable);

	Slice<SneakyViewEntity> findAll(Specification<SneakyViewEntity> spec, Pageable pageable);

	Optional<SneakyViewEntity> findOne(Specification<SneakyViewEntity> spec);

	List<SneakyViewEntity> findAll(Specification<SneakyViewEntity> spec);

	List<SneakyViewEntity> findAll(Specification<SneakyViewEntity> spec, Sort sort);

	long count(Specification<SneakyViewEntity> spec);
}
