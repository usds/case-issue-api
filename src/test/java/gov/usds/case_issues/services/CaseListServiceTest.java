package gov.usds.case_issues.services;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;
import gov.usds.case_issues.test_util.CaseIssueApiTestBase;

public class CaseListServiceTest extends CaseIssueApiTestBase {

	@Autowired
	private CaseListService _service;

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Before
	public void reset() {
		truncateDb();
	}

	@Test
	public void translatePath_invalidCaseManagementSystem_notFoundError() {
		String badId = "NO_SUCH_SYSTEM";
		expected.expect(allOf(
				isA(ApiModelNotFoundException.class),
				hasProperty("entityType", equalTo("Case Management System")),
				hasProperty("entityId", equalTo(badId))
		));
		_service.translatePath(badId, "UNCHECKED");
	}

	@Test
	public void translatePath_invalidCaseType_notFoundError() {
		String goodSystem = "ABCDE";
		String badId = "NOBODY-LOVES-YOU";
		_dataService.ensureCaseManagementSystemInitialized(goodSystem, "Totes Real", "A genuine record in the DB!");
		expected.expect(allOf(
				isA(ApiModelNotFoundException.class),
				hasProperty("entityType", equalTo("Case Type")),
				hasProperty("entityId", equalTo(badId))
		));
		_service.translatePath(goodSystem, badId);
	}

	@Test
	public void translatePath_validPath_itemsFound() {
		String goodSystem = "ABCDE";
		String goodType = "1040E-Z";
		CaseManagementSystem m = _dataService.ensureCaseManagementSystemInitialized(goodSystem, "Fred", null);
		CaseType t = _dataService.ensureCaseTypeInitialized(goodType, "An IRS form", "Look it up");
		CaseGroupInfo translated = _service.translatePath(goodSystem, goodType);
		assertEquals("Case management system ID", m.getCaseManagementSystemId(), translated.getCaseManagementSystemId());
		assertEquals("Case type ID", t.getCaseTypeId(), translated.getCaseTypeId());
	}
}
