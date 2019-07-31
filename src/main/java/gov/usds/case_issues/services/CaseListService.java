package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.model.ApiModelNotFoundException;
import gov.usds.case_issues.model.CaseInformation;

@Service
@Transactional(readOnly=true)
public class CaseListService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseListService.class);

	@Autowired
	private CaseTypeRepository _caseTypeRepo;
	@Autowired
	private CaseManagementSystemRepository _caseManagementSystemRepo;
	@Autowired
	private BulkCaseRepository _bulkRepo;

	public List<CaseInformation> getActiveCases(String caseManagementSystemTag, String caseTypeTag, Pageable pageRequest) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		LOG.debug("Paged request for active cases: {} {}", pageRequest.getPageSize(), pageRequest.getPageNumber());
		Page<Object[]> cases = _bulkRepo.getActiveCases(
			translated.getCaseManagementSystemId(), translated.getCaseTypeId(), pageRequest);
		return rewrap(cases.getContent());
	}

	public List<CaseInformation> getSnoozedCases(String caseManagementSystemTag, String caseTypeTag, Pageable pageRequest) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		LOG.debug("Paged request for snoozed cases: {} {}", pageRequest.getPageSize(), pageRequest.getPageNumber());
		Page<Object[]> cases = _bulkRepo.getSnoozedCases(
			translated.getCaseManagementSystemId(), translated.getCaseTypeId(), pageRequest);
		return rewrap(cases.getContent());
	}

	public Map<String, Number> getSummaryInfo(String caseManagementSystemTag, String caseTypeTag) {
		CaseGroupInfo translated = translatePath(caseManagementSystemTag, caseTypeTag);
		return _bulkRepo.getSnoozeSummary(translated.getCaseManagementSystemId(), translated.getCaseTypeId())
				.stream()
				.collect(Collectors.toMap(a->((String) a[0]).trim(), a->(Number) a[1]));
	}
	
	public CaseGroupInfo translatePath(String caseManagementSystemTag, String caseTypeTag) {
		CaseManagementSystem caseManagementSystem = _caseManagementSystemRepo.findByCaseManagementSystemTag(caseManagementSystemTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Management System", caseManagementSystemTag));
		CaseType caseType = _caseTypeRepo.findByCaseTypeTag(caseTypeTag)
				.orElseThrow(()->new ApiModelNotFoundException("Case Type", caseTypeTag));
		return new CaseGroupInfo(caseManagementSystem, caseType);
	}

	private static List<CaseInformation> rewrap(List<Object[]> queryResult) {
		return queryResult.stream().map(row ->new CaseInformation((TroubleCase) row[0], (ZonedDateTime) row[1]))
			.collect(Collectors.toList());
	}

	private static class CaseGroupInfo {

		private CaseManagementSystem _system;
		private CaseType _type;

		public CaseGroupInfo(CaseManagementSystem _system, CaseType _type) {
			super();
			this._system = _system;
			this._type = _type;
		}

		public Long getCaseManagementSystemId() {
			return _system.getCaseManagementSystemId();
		}

		public Long getCaseTypeId() {
			return _type.getCaseTypeId();
		}

		public CaseManagementSystem getCaseManagementSystem() {
			return _system;
		}

		public CaseType getCaseType() {
			return _type;
		}
	}
}
