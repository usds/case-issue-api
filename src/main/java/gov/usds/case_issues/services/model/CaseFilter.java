package gov.usds.case_issues.services.model;

import org.springframework.data.jpa.domain.Specification;

import gov.usds.case_issues.db.model.reporting.FilterableCase;

/**
 * A possibly ill-advised marker interface for our services to pass to and from one another.
 */
public interface CaseFilter extends Specification<FilterableCase> {

}
