package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
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
import org.springframework.validation.annotation.Validated;

import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.reporting.FilterableCase;
import gov.usds.case_issues.db.repositories.AttachmentAssociationRepository;
import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.db.repositories.reporting.FilterableCaseRepository;
import gov.usds.case_issues.model.AttachmentSummary;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.services.model.CaseFilter;
import gov.usds.case_issues.services.model.CasePageInfo;
import gov.usds.case_issues.services.model.DelegatingFilterableCaseSummary;
import gov.usds.case_issues.validators.TagFragment;

@Service
@Validated
public class CaseFilteringService implements CasePagingService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseFilteringService.class);

	protected static final Sort DEFAULT_SORT = Sort.by(
			Order.asc("caseCreation"), Order.asc("receiptNumber"));
	protected static final Sort SNOOZE_SORT = Sort.by(
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
		List<CaseFilter> filters = new ArrayList<>();
		if (caseCreationRange != null) {
			filters.add(FilterFactory.dateRange(caseCreationRange));
		}
		return getCases(caseManagementSystemTag, caseTypeTag, Collections.singleton(CaseSnoozeFilter.ACTIVE), size, Optional.of(DEFAULT_SORT),
				Optional.ofNullable(receiptNumber), filters);
	}

	@Override
	public List<? extends CaseSummary> getSnoozedCases(String caseManagementSystemTag, String caseTypeTag,
			String receiptNumber, DateRange caseCreationRange, Optional<String> snoozeReason, int size) {
		List<CaseFilter> filters = new ArrayList<>();
		if (caseCreationRange != null) {
			filters.add(FilterFactory.dateRange(caseCreationRange));
		}
		snoozeReason.ifPresent(reason -> filters.add(FilterFactory.snoozeReason(reason)));
		return getCases(caseManagementSystemTag, caseTypeTag, Collections.singleton(CaseSnoozeFilter.SNOOZED), size, Optional.of(SNOOZE_SORT),
				Optional.ofNullable(receiptNumber), filters);
	}

	@Override
	public List<? extends CaseSummary> getPreviouslySnoozedCases(String caseManagementSystemTag, String caseTypeTag,
			String receiptNumber, DateRange caseCreationRange, int size) {
		List<CaseFilter> filters = new ArrayList<>();
		if (caseCreationRange != null) {
			filters.add(FilterFactory.dateRange(caseCreationRange));
		}
		return getCases(caseManagementSystemTag, caseTypeTag, Collections.singleton(CaseSnoozeFilter.ALARMED), size, Optional.of(DEFAULT_SORT),
				Optional.ofNullable(receiptNumber), filters);
	}

	public List<CaseSummary> getCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@NotNull @Size(min=1,max=1) Set<CaseSnoozeFilter> queryFilters,
			@Range(min=1, max=BulkCaseRepository.MAX_PAGE_SIZE) int pageSize,
			@NotNull Optional<Sort> requestedSortOrder,
			@NotNull Optional<String> pageReference,
			@NotNull List<? extends Specification<FilterableCase>> filters
			) {
		Sort sortOrder = requestedSortOrder.orElse(defaultSort(queryFilters)); // in the long run we should probably validate this better.
		CaseSnoozeFilter singleFilter = queryFilters.stream().findFirst().get();
		Specification<FilterableCase> spec = baseSpec(caseManagementSystemTag, caseTypeTag, sortOrder, pageReference)
			.and(caseCategorySpec(singleFilter));
		for (Specification<FilterableCase> f : filters) {
			spec = spec.and(f);
		}
		return wrapFetched(spec, PageRequest.of(0, pageSize, sortOrder));
	}

	private Sort defaultSort(Set<CaseSnoozeFilter> queryFilters) {
		return queryFilters.contains(CaseSnoozeFilter.SNOOZED) ? SNOOZE_SORT : DEFAULT_SORT;
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
					.map(c -> new DelegatingFilterableCaseSummary(c, attachments.get(c.getInternalId())))
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

	private Specification<FilterableCase> baseSpec(String caseManagementSystemTag, String caseTypeTag, Sort sortOrder,
			Optional<String> pageReference) {
		CasePageInfo path = _translator.translatePath(caseManagementSystemTag, caseTypeTag, pageReference.orElse(null)); 
		Specification<FilterableCase> fullSpec = pathSpec(path);
		if (!path.isFirstPage()) {
			fullSpec = fullSpec.and(pageSpec(path.getCase(), sortOrder));
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
}
