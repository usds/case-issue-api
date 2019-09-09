package gov.usds.case_issues.db.repositories;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@Transactional(readOnly=false)
public class TroubleCaseRepositoryTest extends CaseIssueApiTestBase {

	private static final int GIANT_LIST_SIZE = 100_000;
	private static final int LARGE_LIST_SIZE = 29_999;


	@Autowired
	private TroubleCaseRepository _repo;

	@Before
	public void resetDb() {
		truncateDb();
	}

	@Test
	public void getAllByCaseManagementSystemAndReceiptNumberIn_someCasesExist_expectedCasesFound() {
		CaseManagementSystem m1 = _dataService.ensureCaseManagementSystemInitialized("M1", "System 1", null);
		CaseManagementSystem m2 = _dataService.ensureCaseManagementSystemInitialized("M2", "System 2", null);
		CaseType t1 = _dataService.ensureCaseTypeInitialized("T1", "Ahnold", "Terminated");
		ZonedDateTime now = ZonedDateTime.now();
		Set<Long> expectedIds = new HashSet<>();
		expectedIds.add(_dataService.initCase(m1, "F123", t1, now).getInternalId());
		expectedIds.add(_dataService.initCase(m1, "F456", t1, now).getInternalId());
		_dataService.initCase(m1, "F789", t1, now);
		_dataService.initCase(m2, "F123", t1, now);

		Collection<TroubleCase> found = _repo.getAllByCaseManagementSystemAndReceiptNumberIn(
				m1, Arrays.asList("F123", "F456", "F999"));
		assertEquals(2, found.size());
		found.forEach(c -> assertEquals(m1.getInternalId(), c.getCaseManagementSystem().getInternalId()));
		Set<Long> foundIds = found.stream().map(TroubleCase::getInternalId).collect(Collectors.toSet());
		assertEquals(expectedIds, foundIds);
	}

	@Test
	public void getAllByCaseManagementSystemAndReceiptNumberIn_longRequestList_dbOK() {
		CaseManagementSystem m1 = _dataService.ensureCaseManagementSystemInitialized("M1", "System 1", null);
		List<String> receipts = new ArrayList<>(LARGE_LIST_SIZE);
		for (int i = 0; i < LARGE_LIST_SIZE; i++) {
			receipts.add(String.format("FFFF%07d", i));
		}
		_repo.getAllByCaseManagementSystemAndReceiptNumberIn(m1, receipts);
	}

	@Test(expected=ConstraintViolationException.class)
	public void getAllByCaseManagementSystemAndReceiptNumberIn_excessiveRequestList_validationError() {
		CaseManagementSystem m1 = _dataService.ensureCaseManagementSystemInitialized("M1", "System 1", null);
		List<String> receipts = new ArrayList<>(GIANT_LIST_SIZE);
		for (int i = 0; i < GIANT_LIST_SIZE; i++) {
			receipts.add(String.format("FFFF%07d", i));
		}
		_repo.getAllByCaseManagementSystemAndReceiptNumberIn(m1, receipts);
	}
}
