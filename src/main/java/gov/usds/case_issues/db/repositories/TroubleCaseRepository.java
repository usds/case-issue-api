package gov.usds.case_issues.db.repositories;

import java.util.Optional;
import org.springframework.data.repository.PagingAndSortingRepository;

import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;

public interface TroubleCaseRepository extends PagingAndSortingRepository<TroubleCase, Long> {

	public Optional<TroubleCase> getByReceiptNumber(String receiptNumber);

	public Iterable<TroubleCase> getAllByCaseType(CaseType type);

	/* this is a little bit silly looking */
//	public Iterable<TroubleCase> getAllByCaseTypeTypeTagAndCaseTypeCaseManagementSystemCaseManagementSystemTag(String typeCat, String systemTag);
}
