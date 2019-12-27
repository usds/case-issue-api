package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.model.reporting.SneakyViewEntity;
import gov.usds.case_issues.db.repositories.AttachmentAssociationRepository;
import gov.usds.case_issues.db.repositories.SneakyReportRepo;
import gov.usds.case_issues.db.repositories.UserInformationRepository;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.model.NoteSummary;
import gov.usds.case_issues.services.CaseListService.CasePageInfo;

@Service
@Transactional(readOnly=true)
public class SneakyCaseService implements CasePagingService {

	private static final Logger LOG = LoggerFactory.getLogger(SneakyCaseService.class);
	private static final Sort DEFAULT_SORT = Sort.by(
		Order.asc("caseCreation"), Order.asc("receiptNumber"));
	private static final Sort SNOOZE_SORT = Sort.by(
		Order.asc("snoozeEnd"), Order.asc("caseCreation"), Order.asc("receiptNumber"));

	@Autowired
	private PageTranslationService _translator;
	@Autowired
	private SneakyReportRepo _repo;
	@Autowired
	private AttachmentAssociationRepository _attachmentAssociationRepo;
	@Autowired
	private UserInformationRepository _userInformationRepo;

	@Override
	public List<CaseSummary> getActiveCases(String caseManagementSystemTag, String caseTypeTag, String receiptNumber,
			DateRange caseCreationRange, int size) {
		Pageable sortOrder = activeCasePage(size);
		Specification<SneakyViewEntity> pathSpec = commonSpec(caseManagementSystemTag, caseTypeTag,
				sortOrder.getSort(),
				Optional.ofNullable(receiptNumber), Optional.ofNullable(caseCreationRange));
		Specification<SneakyViewEntity> notSnoozed = (root, query, cb) -> cb.or(
			cb.lessThan(root.get("snoozeEnd"), ZonedDateTime.now()),
			cb.isNull(root.get("snoozeEnd"))
		);
		return wrapFetched(
			Specification.where(pathSpec.and(notSnoozed)),
			sortOrder
		);
	}

	@Override
	public List<CaseSummary> getSnoozedCases(String caseManagementSystemTag, String caseTypeTag, String receiptNumber,
			DateRange caseCreationRange, Optional<String> snoozeReason, int size) {
		Pageable pageInfo = snoozedCasePage(size);
		Specification<SneakyViewEntity> mainSpec = commonSpec(caseManagementSystemTag, caseTypeTag,
				pageInfo.getSort(), Optional.ofNullable(receiptNumber), Optional.ofNullable(caseCreationRange));
		Specification<SneakyViewEntity> snoozed = (root, query, cb) -> 
			cb.greaterThanOrEqualTo(root.get("snoozeEnd"), ZonedDateTime.now());
		mainSpec = mainSpec.and(snoozed);
		if (snoozeReason.isPresent()) {
			Specification<SneakyViewEntity> snoozeReasonSpec = (root, query, cb) ->
				cb.equal(root.get("snoozeReason"), snoozeReason.get());
			mainSpec = mainSpec.and(snoozeReasonSpec);
		}
		return wrapFetched(mainSpec, pageInfo);
	}

	private List<CaseSummary> wrapFetched(Specification<SneakyViewEntity> mainSpec, Pageable pageInfo) {
		try {
			LOG.debug("Starting fetch using {}/{}", mainSpec, pageInfo);
			Slice<SneakyViewEntity> page = _repo.findAll(mainSpec, pageInfo);
			LOG.debug("Finished fetch using {}/{}", mainSpec, pageInfo);
			List<SneakyViewEntity> cases = page.getContent();
			List<Long> caseIds = cases.stream().map(SneakyViewEntity::getInternalId).collect(Collectors.toList());
			List<CaseAttachmentAssociation> associations = _attachmentAssociationRepo.findAllBySnoozeSnoozeCaseInternalIdIn(caseIds);
			Map<Long, List<NoteSummary>> attachments = new HashMap<>();
			for (CaseAttachmentAssociation assoc : associations) {
				Long caseId = assoc.getSnooze().getSnoozeCase().getInternalId();
				List<NoteSummary> attachmentList = attachments.get(caseId);
				if (attachmentList == null) {
					attachmentList = new ArrayList<>();
					attachments.put(caseId, attachmentList);
				}
				UserInformation userInfo = null;
				if (assoc.getCreatedBy() != null) {
					userInfo = _userInformationRepo.findByUserId(assoc.getCreatedBy());
				}
				attachmentList.add(new NoteSummary(assoc, userInfo));
			}
			return cases.stream()
					.map(c -> new DelegatingSummary(c, attachments.get(c.getInternalId())))
					.collect(Collectors.toList());
		} catch (InvalidDataAccessApiUsageException e) {
			if (e.getCause() instanceof IllegalArgumentException) {
				throw (IllegalArgumentException) e.getCause();
			} else {
				throw e;
			}
		}
	}

	@Override
	public List<CaseSummary> getPreviouslySnoozedCases(String caseManagementSystemTag, String caseTypeTag,
			String receiptNumber, DateRange caseCreationRange, int size) {
		Pageable pageInfo = activeCasePage(size);
		Specification<SneakyViewEntity> pathSpec = commonSpec(caseManagementSystemTag, caseTypeTag,
				pageInfo.getSort(), Optional.ofNullable(receiptNumber), Optional.ofNullable(caseCreationRange));
		Specification<SneakyViewEntity> wasSnoozed = (root, query, cb) -> 
			cb.lessThan(root.get("snoozeEnd"), ZonedDateTime.now());
		return wrapFetched(Specification.where(pathSpec.and(wasSnoozed)), pageInfo);
	}

	private static Pageable activeCasePage(int size) {
		return PageRequest.of(0, size, DEFAULT_SORT);
	}

	private static Pageable snoozedCasePage(int size) {
		return PageRequest.of(0, size, SNOOZE_SORT);
	}

	private Specification<SneakyViewEntity> commonSpec(
			String caseManagementSystemTag,
			String caseTypeTag,
			Sort sortOrder,
			Optional<String> pageReference, Optional<DateRange> caseCreationRange) {
		CasePageInfo path = _translator.translatePath(caseManagementSystemTag, caseTypeTag, pageReference.orElse(null)); 
		Specification<SneakyViewEntity> fullSpec = pathSpec(path);
		if (!path.isFirstPage()) {
			fullSpec = fullSpec.and(pageSpec(path.getCase(), sortOrder));
		}
		if (caseCreationRange.isPresent()) {
			DateRange dateRange = caseCreationRange.get();
			Specification<SneakyViewEntity> dateSpec = (root, query, cb) -> cb.and(
				cb.greaterThanOrEqualTo(root.get("caseCreation"), dateRange.getStartDate()),
				cb.lessThanOrEqualTo(root.get("caseCreation"), dateRange.getEndDate())
			);
			fullSpec = fullSpec.and(dateSpec);
		}
		return fullSpec;
	}

	private Specification<SneakyViewEntity> pageSpec(TroubleCase troubleCase, Sort sortOrder) {
		// oh ugh....
		SneakyViewEntity snuck = _repo.findById(troubleCase.getInternalId()).get();
		Map<String, Function<SneakyViewEntity, Comparable<?>>> lookup = new HashMap<>();
		lookup.put("caseCreation", SneakyViewEntity::getCaseCreation);
		lookup.put("receiptNumber", SneakyViewEntity::getReceiptNumber);
		lookup.put("snoozeEnd", e -> { // slightly gross workaround
			ZonedDateTime snoozeEnd = e.getSnoozeEnd();
			if (null == snoozeEnd || snoozeEnd.isBefore(ZonedDateTime.now())) {
				throw new IllegalArgumentException("Page reference case was not snoozed");
			}
			return snoozeEnd;
		});

		@SuppressWarnings({"rawtypes", "unchecked"})
		Specification<SneakyViewEntity> keySet = (root, query, cb) -> {
			List<Predicate> priorFields = new ArrayList<>();
			List<Predicate> alternates = new ArrayList<>();
			for (Order o : sortOrder) {
				String property = o.getProperty();
				Comparable val = lookup.get(property).apply(snuck);
				if (val == null) {
					throw new IllegalArgumentException("Property " + property + " cannot be found via page reference");
				}
				List<Predicate> conjunction = new ArrayList<>(priorFields);
				Path<Comparable> field = root.get(property);
				Expression<Comparable> placeholder = cb.literal(val);
				conjunction.add(o.isAscending() ? cb.greaterThan(field, placeholder) : cb.lessThan(field, placeholder));
				alternates.add(cb.and(conjunction.toArray(new Predicate[conjunction.size()])));
				priorFields.add(cb.equal(field, placeholder));	
			}
			return cb.or(alternates.toArray(new Predicate[alternates.size()]));
		};
		return keySet;
	}

	private Specification<SneakyViewEntity> pathSpec(CasePageInfo path) {
		return (root1, query1, cb1) -> cb1.and(
			cb1.equal(root1.get("caseManagementSystem"), path.getCaseManagementSystem()),
			cb1.equal(root1.get("caseType"), path.getCaseType()),
			// this should probably do the subquery directly rather than using the one in the view
			cb1.isTrue(root1.get("hasOpenIssue"))
		);
	}

	private static class DelegatingSummary implements CaseSummary {
		private SneakyViewEntity _root;
		private List<NoteSummary> _attachments;

		public DelegatingSummary(SneakyViewEntity r, List<NoteSummary> attachments) {
			_root = r;
			_attachments = attachments;
			if (attachments == null) {
				_attachments = Collections.emptyList();
			}
		}
		@Override
		public String getReceiptNumber() {
			return _root.getReceiptNumber();
		}
		@Override
		public ZonedDateTime getCaseCreation() {
			return _root.getCaseCreation();
		}
		@Override
		public Map<String, Object> getExtraData() {
			return _root.getExtraData();
		}
		@Override
		public boolean isPreviouslySnoozed() {
			return false;
		}
		@Override
		public CaseSnoozeSummary getSnoozeInformation() {
			return new CaseSnoozeSummary() {
				@Override
				public ZonedDateTime getSnoozeStart() {
					return _root.getSnoozeStart();
				}
				
				@Override
				public String getSnoozeReason() {
					return _root.getSnoozeReason();
				}
				
				@Override
				public ZonedDateTime getSnoozeEnd() {
					return _root.getSnoozeEnd();
				}
				
				@Override
				public String getCreatedBy() {
					return _root.getCreatedBy();
				}
			};
		}
		@Override
		public List<NoteSummary> getNotes() {
			return _attachments;
		}

	}
	
}
