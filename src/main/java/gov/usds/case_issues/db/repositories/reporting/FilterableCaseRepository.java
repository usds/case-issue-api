package gov.usds.case_issues.db.repositories.reporting;

import gov.usds.case_issues.db.model.reporting.FilterableCase;
import gov.usds.case_issues.db.repositories.TroubleCaseFixedDataRepository;

/**
 * Specialization of {@link TroubleCaseFixedDataRepository} for {@link FilterableCase} retrieval.
 */
public interface FilterableCaseRepository extends TroubleCaseFixedDataRepository<FilterableCase> {

}
