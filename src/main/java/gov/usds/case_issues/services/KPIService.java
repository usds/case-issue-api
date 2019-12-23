package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;
import gov.usds.case_issues.validators.TagFragment;

@Service
public class KPIService {
	private static final ZonedDateTime START = ZonedDateTime.parse("2019-10-21T00:00:00.000Z");
	private static final Logger LOG = LoggerFactory.getLogger(IssueUploadService.class);

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
		kpis.put(
			"ResolvedTickets",
			getResolvedTickets(translated.getCaseManagementSystemId(), translated.getCaseTypeId())
		);
		return kpis;
	}

	private List<Integer> getResolvedTickets(Long caseManagementSystemId, Long caseTypeId) {
		ArrayList<Integer> ticketsResolved = new ArrayList<Integer>();
		// between(inclusve, exclusive)
		ZonedDateTime now = ZonedDateTime.now();
		Long weeks = ChronoUnit.WEEKS.between(START, now);
		for (int i = 0; i < weeks; i++) {
			ZonedDateTime start = START.plusWeeks(i);
			ZonedDateTime end = START.plusWeeks(i + 1);
			LOG.error("{}/{}, {}, {}", i, weeks, start, end);
			if (end.isAfter(now)) {
				break;
			}
			Integer resolved = _bulkRepo.getResolvedCaseCount(caseManagementSystemId, caseTypeId, start, end);
			ticketsResolved.add(i, resolved);
		}
		return ticketsResolved;
	}
}

