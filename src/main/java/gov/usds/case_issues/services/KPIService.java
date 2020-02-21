package gov.usds.case_issues.services;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.services.model.CaseGroupInfo;
import gov.usds.case_issues.validators.TagFragment;

@Service
@Validated
public class KPIService {
	private static final int WEEKS = 10;
	private static TemporalAdjuster START_OF_WEEK = TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY);

	@Autowired
	private BulkCaseRepository _bulkRepo;
	@Autowired
	private CaseListService _caseListService;

	public Map<String, Object> getKPIData(
		@TagFragment String caseManagementSystemTag,
		@TagFragment String caseTypeTag,
		ZonedDateTime start
	) {
		CaseGroupInfo translated = _caseListService.translatePath(
			caseManagementSystemTag,
			caseTypeTag
		);
		HashMap<String, Object> kpis = new HashMap<String, Object>();
		Long caseManagementSystemId = translated.getCaseManagementSystemId();
		Long caseTypeId = translated.getCaseTypeId();
		ZonedDateTime startOfWeek = start.with(START_OF_WEEK);
		kpis.put("ResolvedTickets", getResolvedTickets(caseManagementSystemId, caseTypeId, startOfWeek));
		kpis.put("DaysToResolution", getAverageDaysToResolution(caseManagementSystemId, caseTypeId, startOfWeek));
		kpis.put("DaysWorked", getAverageDaysWorked(caseManagementSystemId, caseTypeId, startOfWeek));
		return kpis;
	}

	private List<Integer> getResolvedTickets(Long caseManagementSystemId, Long caseTypeId, ZonedDateTime rangeStart) {
		ArrayList<Integer> ticketsResolved = new ArrayList<Integer>();
		for (int i = 0; i < WEEKS; i++) {
			ZonedDateTime start = rangeStart.minusWeeks(i +1);
			ZonedDateTime end = rangeStart.minusWeeks(i);
			int resolved = _bulkRepo.getResolvedCaseCount(caseManagementSystemId, caseTypeId, start, end);
			ticketsResolved.add(i, resolved);
		}
		return ticketsResolved;
	}

	private List<Integer> getAverageDaysToResolution(Long caseManagementSystemId, Long caseTypeId, ZonedDateTime rangeStart) {
		ArrayList<Integer> daysToResolution = new ArrayList<Integer>();
		for (int i = 0; i < WEEKS; i++) {
			ZonedDateTime start = rangeStart.minusWeeks(i +1);
			ZonedDateTime end = rangeStart.minusWeeks(i);
			int days = _bulkRepo.getAverageDaysToResolution(caseManagementSystemId, caseTypeId, start, end);
			daysToResolution.add(i, days);
		}
		return daysToResolution;
	}

	private List<Integer> getAverageDaysWorked(Long caseManagementSystemId, Long caseTypeId, ZonedDateTime rangeStart) {
		ArrayList<Integer> daysToResolution = new ArrayList<Integer>();
		for (int i = 0; i < WEEKS; i++) {
			ZonedDateTime start = rangeStart.minusWeeks(i +1);
			ZonedDateTime end = rangeStart.minusWeeks(i);
			int days = _bulkRepo.getAverageDaysWorked(caseManagementSystemId, caseTypeId, start, end);
			daysToResolution.add(i, days);
		}
		return daysToResolution;
	}
}

