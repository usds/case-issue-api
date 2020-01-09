package gov.usds.case_issues.db.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import javax.validation.constraints.Size;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCaseFixedData;

@NoRepositoryBean
public interface TroubleCaseFixedDataRepository<T extends TroubleCaseFixedData> extends Repository<T, Long>{

	public static final int MAX_INLIST_SIZE = 32000;
	public static final String INLIST_SIZE_MESSAGE = "Too many items in this IN-list: not all databases can handle this many placeholders.";

	public Page<T> getAllByCaseManagementSystemAndCaseType(CaseManagementSystem caseManager, CaseType caseType, Pageable pageable);

	public Optional<T> findByCaseManagementSystemAndReceiptNumber(CaseManagementSystem caseManager, String receiptNumber);

	public List<T> getFirst5ByCaseManagementSystemAndCaseTypeAndReceiptNumberContains(CaseManagementSystem caseManager, CaseType caseType, String receiptNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE) // might need to be more aggressive when postgresql table-level LOCK is available
	public Collection<T> getAllByCaseManagementSystemAndReceiptNumberIn(CaseManagementSystem caseManager,
			@Size(max=MAX_INLIST_SIZE, message=INLIST_SIZE_MESSAGE) Collection<String> receiptNumbers);

}
