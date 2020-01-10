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
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;

import org.json.JSONObject;
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

import gov.usds.case_issues.db.JsonOperatorContributor;
import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.projections.CaseSnoozeSummary;
import gov.usds.case_issues.db.model.reporting.FilterableCase;
import gov.usds.case_issues.db.repositories.AttachmentAssociationRepository;
import gov.usds.case_issues.db.repositories.reporting.FilterableCaseRepository;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.services.model.CasePageInfo;

@Service
public class CaseFilteringService implements CasePagingService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseFilteringService.class);

	private static final Sort DEFAULT_SORT = Sort.by(
			Order.asc("caseCreation"), Order.asc("receiptNumber"));
	private static final Sort SNOOZE_SORT = Sort.by(
		Order.asc("snoozeEnd"), Order.asc("caseCreation"), Order.asc("receiptNumber"));

	@Autowired
	private FilterableCaseRepository _repo;
	@Autowired
	private AttachmentAssociationRepository _attachmentAssociationRepo;
	@Autowired
	private PageTranslationService _translator;

	@Override
	public List<? extends CaseSummary> getActiveCases(String caseManagementSystemTag, String caseTypeTag,
			String receiptNumber, DateRange caseCreationRange, int size) {
		return getCases(CaseSnoozeFilter.ACTIVE, caseManagementSystemTag, caseTypeTag, DEFAULT_SORT, Optional.ofNullable(receiptNumber), size, Optional.ofNullable(caseCreationRange),
				Optional.empty(), Collections.emptyMap(), Optional.empty());
	}

	@Override
	public List<? extends CaseSummary> getSnoozedCases(String caseManagementSystemTag, String caseTypeTag,
			String receiptNumber, DateRange caseCreationRange, Optional<String> snoozeReason, int size) {
		return getCases(CaseSnoozeFilter.SNOOZED, caseManagementSystemTag, caseTypeTag, SNOOZE_SORT, Optional.ofNullable(receiptNumber), size,
				Optional.ofNullable(caseCreationRange),
				snoozeReason, Collections.emptyMap(), Optional.empty());
	}

	@Override
	public List<? extends CaseSummary> getPreviouslySnoozedCases(String caseManagementSystemTag, String caseTypeTag,
			String receiptNumber, DateRange caseCreationRange, int size) {
		return getCases(CaseSnoozeFilter.ALARMED, caseManagementSystemTag, caseTypeTag, DEFAULT_SORT, Optional.ofNullable(receiptNumber), size,
				Optional.ofNullable(caseCreationRange),
				Optional.empty(), Collections.emptyMap(), Optional.empty());
	}

	public List<CaseSummary> getCases(CaseSnoozeFilter queryFilter, String caseManagementSystemTag, String caseTypeTag, 
			Sort sortOrder, Optional<String> pageReference, int pageSize,
			@NotNull Optional<DateRange> caseCreationRange,
			@NotNull Optional<String> snoozeReason,
			@NotNull Map<String, Object> fieldFilter,
			@NotNull Optional<AttachmentRequest> attachmentFilter
			) {
		if (sortOrder == null) {
			sortOrder = DEFAULT_SORT;
		}
		Specification<FilterableCase> mainSpec = commonSpec(caseManagementSystemTag, caseTypeTag, sortOrder,
				pageReference, caseCreationRange);
		if (queryFilter != null) {
			mainSpec = mainSpec.and(caseCategorySpec(queryFilter));
		}
		if (snoozeReason.isPresent()) {
			mainSpec = mainSpec.and((root, query, cb) -> cb.equal(root.get("snoozeReason"), snoozeReason.get()));
		}
		if (fieldFilter != null && fieldFilter.size() > 0) {
			mainSpec = mainSpec.and(caseExtraDataSpec(fieldFilter));
		}
		if (attachmentFilter.isPresent()) {
			mainSpec = mainSpec.and(attachmentSpec(attachmentFilter.get()));
		}
		return wrapFetched(mainSpec, PageRequest.of(0, pageSize, sortOrder));
	}

	private Specification<FilterableCase> attachmentSpec(AttachmentRequest attachmentRequest) {
		return (root, query, cb) -> {
			Subquery<CaseAttachmentAssociation> sq = query.subquery(CaseAttachmentAssociation.class);
			List<Predicate> conjunction = new ArrayList<>();
			Root<CaseAttachmentAssociation> aRoot = sq.from(CaseAttachmentAssociation.class);
			sq.select(aRoot);
			Path<CaseAttachment> aPath = aRoot.get("attachment");
			conjunction.add(cb.equal(aPath.get("attachmentType"), cb.literal(attachmentRequest.getNoteType())));
			if (attachmentRequest.getSubtype() != null) {
				conjunction.add(cb.equal(aPath.get("attachmentSubtype").get("externalId"), cb.literal(attachmentRequest.getSubtype())));
			}
			if (attachmentRequest.getContent() != null) {
				conjunction.add(cb.equal(aPath.get("content"), cb.literal(attachmentRequest.getContent())));

			}
			conjunction.add(cb.equal(aRoot.get("snooze").get("snoozeCase").get("internalId"), root.get("internalId")));
			sq.where(conjunction.toArray(new Predicate[0]));
			return cb.exists(sq);
		};
	}

	private Specification<FilterableCase> caseExtraDataSpec(Map<String, Object> fieldFilter) {
		String jsonQuery = new JSONObject(fieldFilter).toString();
		return (root, query, cb) -> cb.isTrue(
				cb.function(JsonOperatorContributor.JSON_CONTAINS, Boolean.class, root.get("extraData"), cb.literal(jsonQuery)));
	}

	private Specification<FilterableCase> caseCategorySpec(CaseSnoozeFilter queryFilter) {
		switch (queryFilter) {
			case ACTIVE:
				return (root, query, cb) -> cb.or(
						cb.lessThan(root.get("snoozeEnd"), cb.currentTimestamp()),
						cb.isNull(root.get("snoozeEnd"))
				);
			case SNOOZED:
				return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("snoozeEnd"), cb.currentTimestamp());
			case ALARMED:
				return (root, query, cb) -> cb.lessThan(root.get("snoozeEnd"), cb.currentTimestamp());
			case UNCHECKED:
				return (root, query, cb) -> cb.isNull(root.get("snoozeEnd"));
			default:
				throw new IllegalArgumentException();
		}
	}

	private List<CaseSummary> wrapFetched(Specification<FilterableCase> mainSpec, Pageable pageInfo) {
		try {
			LOG.debug("Starting fetch using {}/{}", mainSpec, pageInfo);
			Slice<FilterableCase> page = _repo.findAll(mainSpec, pageInfo);
			LOG.debug("Finished fetch using {}/{}", mainSpec, pageInfo);
			List<FilterableCase> cases = page.getContent();
			Map<Long, List<AttachmentSummary>> attachments = fetchAllAttachments(cases);
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

	private Map<Long, List<AttachmentSummary>> fetchAllAttachments(List<FilterableCase> cases) {
		List<Long> caseIds = cases.stream().map(FilterableCase::getInternalId).collect(Collectors.toList());
		List<CaseAttachmentAssociation> associations = _attachmentAssociationRepo.findAllBySnoozeSnoozeCaseInternalIdIn(caseIds);
		Map<Long, List<AttachmentSummary>> attachments = new HashMap<>();
		for (CaseAttachmentAssociation assoc : associations) {
			Long caseId = assoc.getSnooze().getSnoozeCase().getInternalId();
			List<AttachmentSummary> attachmentList = attachments.get(caseId);
			if (attachmentList == null) {
				attachmentList = new ArrayList<>();
				attachments.put(caseId, attachmentList);
			}
			attachmentList.add(new AttachmentSummary(assoc));
		}
		return attachments;
	}

	private Specification<FilterableCase> commonSpec(
			String caseManagementSystemTag,
			String caseTypeTag,
			Sort sortOrder,
			Optional<String> pageReference, Optional<DateRange> caseCreationRange) {
		CasePageInfo path = _translator.translatePath(caseManagementSystemTag, caseTypeTag, pageReference.orElse(null)); 
		Specification<FilterableCase> fullSpec = pathSpec(path);
		if (!path.isFirstPage()) {
			fullSpec = fullSpec.and(pageSpec(path.getCase(), sortOrder));
		}
		if (caseCreationRange.isPresent()) {
			DateRange dateRange = caseCreationRange.get();
			Specification<FilterableCase> dateSpec = (root, query, cb) -> cb.and(
				cb.greaterThanOrEqualTo(root.get("caseCreation"), dateRange.getStartDate()),
				cb.lessThanOrEqualTo(root.get("caseCreation"), dateRange.getEndDate())
			);
			fullSpec = fullSpec.and(dateSpec);
		}
		return fullSpec;
	}

	private Specification<FilterableCase> pageSpec(FilterableCase troubleCase, Sort sortOrder) {
		FilterableCase snuck = troubleCase;
		Map<String, Function<FilterableCase, Comparable<?>>> lookup = new HashMap<>();
		lookup.put("caseCreation", FilterableCase::getCaseCreation);
		lookup.put("receiptNumber", FilterableCase::getReceiptNumber);
		lookup.put("snoozeEnd", e -> { // slightly gross workaround
			ZonedDateTime snoozeEnd = e.getSnoozeEnd();
			if (null == snoozeEnd || snoozeEnd.isBefore(ZonedDateTime.now())) {
				throw new IllegalArgumentException("Page reference case was not snoozed");
			}
			return snoozeEnd;
		});

		@SuppressWarnings({"rawtypes", "unchecked"})
		Specification<FilterableCase> keySet = (root, query, cb) -> {
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

	private Specification<FilterableCase> pathSpec(CasePageInfo path) {
		return (root1, query1, cb1) -> cb1.and(
			cb1.equal(root1.get("caseManagementSystem"), path.getCaseManagementSystem()),
			cb1.equal(root1.get("caseType"), path.getCaseType()),
			// this should probably do the subquery directly rather than using the one in the view
			cb1.isTrue(root1.get("hasOpenIssue"))
		);
	}

	private static class DelegatingSummary implements CaseSummary {
		private FilterableCase _root;
		private List<AttachmentSummary> _attachments;

		public DelegatingSummary(FilterableCase r, List<AttachmentSummary> attachments) {
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
			};
		}
		@Override
		public List<AttachmentSummary> getNotes() {
			return _attachments;
		}
	}
	
	
}
