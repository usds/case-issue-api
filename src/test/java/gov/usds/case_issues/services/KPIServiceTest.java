package gov.usds.case_issues.services;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

@SuppressWarnings("checkstyle:MagicNumber")
public class KPIServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private KPIService _KPIService;

	private CaseManagementSystem _system;
	private CaseType _type;
	private static final ZonedDateTime START_DATE = ZonedDateTime.now();
	private ArrayList<Integer> _emptyArray;

	@Before
	public void reset() {
		truncateDb();
		_system = _dataService.ensureCaseManagementSystemInitialized("BIPPITY", "Fred", null);
		_type = _dataService.ensureCaseTypeInitialized("BOPPITY", "An IRS form", "Look it up");
		_emptyArray = new ArrayList<Integer>();
		Collections.addAll(_emptyArray, 0,0,0,0,0,0,0,0,0,0);
	}

	@Test
	public void getKPIData_noData_allValuseZero() {
		Map<String, Object> uploaded = _KPIService.getKPIData("BIPPITY", "BOPPITY");

		assertEquals(_emptyArray, uploaded.get("ResolvedTickets"));
		assertEquals(_emptyArray, uploaded.get("DaysToResolution"));
		assertEquals(_emptyArray, uploaded.get("DaysWorked"));
	}

	@Test
	public void getKPIData_oneResolvedCase_isReported() {
		TroubleCase tc = _dataService.initCaseAndIssue(
			_system,
			"1234567890",
			_type,
			START_DATE.minusDays(14),
			"PAGING",
			START_DATE.minusDays(3)
		);
		_dataService.snoozeCase(tc, "abc", 3, true);
		ArrayList<Integer> expectedResolvedTickets = new ArrayList<Integer>();
		Collections.addAll(expectedResolvedTickets, 1,0,0,0,0,0,0,0,0,0);

		ArrayList<Integer> expectedDaysToResolution = new ArrayList<Integer>();
		Collections.addAll(expectedDaysToResolution, 11,0,0,0,0,0,0,0,0,0);

		ArrayList<Integer> expectedDaysWorked = new ArrayList<Integer>();
		Collections.addAll(expectedDaysWorked, -3,0,0,0,0,0,0,0,0,0);

		Map<String, Object> uploaded = _KPIService.getKPIData("BIPPITY", "BOPPITY");

		assertEquals(expectedResolvedTickets, uploaded.get("ResolvedTickets"));
		assertEquals(expectedDaysToResolution, uploaded.get("DaysToResolution"));
		assertEquals(expectedDaysWorked, uploaded.get("DaysWorked"));
	}
}
