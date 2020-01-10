package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;
import gov.usds.case_issues.validators.TagFragment;

@Service
public class KPIService {
	private static final int WEEKS = 10;

	@Autowired
	private BulkCaseRepository _bulkRepo;
	@Autowired
	private CaseListService _caseListService;

	public Map<String, Object> getKPIData(
		@TagFragment String caseManagementSystemTag,
		@TagFragment String caseTypeTag
	) {
		CaseGroupInfo translated = _caseListService.translatePath(
			caseManagementSystemTag,
			caseTypeTag
		);
		HashMap<String, Object> kpis = new HashMap<String, Object>();
		Long caseManagementSystemId = translated.getCaseManagementSystemId();
		Long caseTypeId = translated.getCaseTypeId();
		kpis.put("ResolvedTickets", getResolvedTickets(caseManagementSystemId, caseTypeId));
		kpis.put("DaysToResolution", getAverageDaysToResolution(caseManagementSystemId, caseTypeId));
		kpis.put("DaysWorked", getAverageDaysWorked(caseManagementSystemId, caseTypeId));
		return kpis;
	}

	private List<Integer> getResolvedTickets(Long caseManagementSystemId, Long caseTypeId) {
		ArrayList<Integer> ticketsResolved = new ArrayList<Integer>();
		ZonedDateTime now = ZonedDateTime.now();
		for (int i = 0; i < WEEKS; i++) {
			ZonedDateTime start = now.minusWeeks(i +1);
			ZonedDateTime end = now.minusWeeks(i);
			int resolved = _bulkRepo.getResolvedCaseCount(caseManagementSystemId, caseTypeId, start, end);
			ticketsResolved.add(i, resolved);
		}
		return ticketsResolved;
	}

	private List<Integer> getAverageDaysToResolution(Long caseManagementSystemId, Long caseTypeId) {
		ArrayList<Integer> daysToResolution = new ArrayList<Integer>();
		ZonedDateTime now = ZonedDateTime.now();
		for (int i = 0; i < WEEKS; i++) {
			ZonedDateTime start = now.minusWeeks(i +1);
			ZonedDateTime end = now.minusWeeks(i);
			int days = _bulkRepo.getAverageDaysToResolution(caseManagementSystemId, caseTypeId, start, end);
			daysToResolution.add(i, days);
		}
		return daysToResolution;
	}

	private List<Integer> getAverageDaysWorked(Long caseManagementSystemId, Long caseTypeId) {
		ArrayList<Integer> daysToResolution = new ArrayList<Integer>();
		ZonedDateTime now = ZonedDateTime.now();
		for (int i = 0; i < WEEKS; i++) {
			ZonedDateTime start = now.minusWeeks(i +1);
			ZonedDateTime end = now.minusWeeks(i);
			int days = _bulkRepo.getAverageDaysWorked(caseManagementSystemId, caseTypeId, start, end);
			daysToResolution.add(i, days);
		}
		return daysToResolution;
	}
}

