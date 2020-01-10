package gov.usds.case_issues.db.repositories.reporting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import gov.usds.case_issues.db.model.reporting.FilterableCase;
import gov.usds.case_issues.db.repositories.TroubleCaseFixedDataRepository;

/**
 * Specialization of {@link TroubleCaseFixedDataRepository} for {@link FilterableCase} retrieval.
 */
public interface FilterableCaseRepository extends TroubleCaseFixedDataRepository<FilterableCase> {

	Slice<FilterableCase> findAll(Specification<FilterableCase> spec, Pageable pageable);

	Optional<FilterableCase> findOne(Specification<FilterableCase> spec);

	List<FilterableCase> findAll(Specification<FilterableCase> spec);

	List<FilterableCase> findAll(Specification<FilterableCase> spec, Sort sort);

	long count(Specification<FilterableCase> spec);
}
