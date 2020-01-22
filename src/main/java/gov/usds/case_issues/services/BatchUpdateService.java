package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.BatchUpdateRequestErrors;
import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.TroubleCaseFixedData;
import gov.usds.case_issues.db.model.reporting.FilterableCase;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.db.repositories.reporting.FilterableCaseRepository;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.BatchUpdateAction;
import gov.usds.case_issues.model.BatchUpdateRequest;
import gov.usds.case_issues.model.BatchUpdateRequestException;
import gov.usds.case_issues.services.model.CaseGroupInfo;
import gov.usds.case_issues.validators.TagFragment;

@Service
@Transactional(readOnly=false)
public class BatchUpdateService {

	@Autowired
	private PageTranslationService _pageService;
	@Autowired
	private CaseAttachmentService _attachmentService;
	@Autowired
	private FilterableCaseRepository _filterableCaseRepo;
	@Autowired
	private TroubleCaseRepository _mainCaseRepo;
	@Autowired
	private CaseSnoozeRepository _snoozeRepo;

	public List<CaseAttachment> processBatchAction(@TagFragment String caseManagementSystemTag, @TagFragment String caseTypeTag,
			BatchUpdateRequest request) {
		CaseGroupInfo path = _pageService.translatePath(caseManagementSystemTag, caseTypeTag);
		Collection<FilterableCase> cases = _filterableCaseRepo.getAllByCaseManagementSystemAndReceiptNumberIn(
				path.getCaseManagementSystem(), request.getReceiptNumbers());
		validateRequest(request, path, cases);

		switch (request.getUpdateAction()) {
			case ATTACH:
				return doAttachNotes(cases, request.getAttachments());
			case BEGIN_SNOOZE:
				return doSnoozeCases(cases, request);
			case END_SNOOZE:
				return doEndSnoozes(cases, request);
			default:
				throw new IllegalArgumentException("Unsupported batch action " + request.getUpdateAction());
		}
	}

	private List<CaseAttachment> doAttachNotes(Collection<FilterableCase> cases, List<AttachmentRequest> attachments) {
		Collection <CaseSnooze> snoozes = getSnoozes(cases);
		return attachAll(attachments, snoozes);
	}

	private List<CaseAttachment> doEndSnoozes(Collection<FilterableCase> cases, BatchUpdateRequest request) {
		Collection <CaseSnooze> snoozes = getSnoozes(cases);
		CaseAttachment correlationId = _attachmentService.attachToSnoozes(
			getCorrelationId(BatchUpdateAction.END_SNOOZE, snoozes.size()), snoozes);
		snoozes.stream().forEach(CaseSnooze::endSnoozeNow); // query: should we pass in "now" so it's identical?
		List<CaseAttachment> newAttachments = new ArrayList<>();
		newAttachments.add(correlationId);
		newAttachments.addAll(attachAll(request.getAttachments(), snoozes));
		return newAttachments;
	}

	private List<CaseAttachment> doSnoozeCases(Collection<FilterableCase> cases, BatchUpdateRequest batchRequest) {
		String snoozeReason = batchRequest.getSnoozeReason().orElseThrow(() -> new IllegalArgumentException("Snooze reason must be provided"));
		int duration = batchRequest.getDuration();
		List<Long> caseIds = cases.stream().map(TroubleCaseFixedData::getInternalId).collect(Collectors.toList());
		Collection<CaseSnooze> snoozes = new ArrayList<>();
		Collection<CaseSnooze> saved = new ArrayList<>();
		_mainCaseRepo.findAllById(caseIds).forEach(c -> snoozes.add(new CaseSnooze(c, snoozeReason, duration)));
		_snoozeRepo.saveAll(snoozes).forEach(saved::add);
		CaseAttachment correlationId = _attachmentService.attachToSnoozes(getCorrelationId(BatchUpdateAction.BEGIN_SNOOZE, cases.size()), saved);
		List<CaseAttachment> newAttachments = new ArrayList<>();
		newAttachments.add(correlationId);
		newAttachments.addAll(attachAll(batchRequest.getAttachments(), snoozes));
		return newAttachments;
	}

	private List<CaseAttachment> attachAll(List<AttachmentRequest> attachments, Collection<CaseSnooze> snoozes) {
		return attachments.stream()
			.map(req -> _attachmentService.attachToSnoozes(req, snoozes))
			.collect(Collectors.toList());
	}

	private AttachmentRequest getCorrelationId(BatchUpdateAction action, int count) {
		JSONObject message = new JSONObject();
		message.put("action", action.name());
		message.put("cases", count);
		message.put("timestamp", ZonedDateTime.now().toString());
		return new AttachmentRequest(AttachmentType.CORRELATION_ID, message.toString());
	}

	private Collection<CaseSnooze> getSnoozes(Collection<FilterableCase> cases) {
		List<Long> snoozeIds = cases.stream().map(FilterableCase::getSnoozeId).collect(Collectors.toList());
		return _snoozeRepo.findAllById(snoozeIds);
	}

	protected static void validateRequest(BatchUpdateRequest request, CaseGroupInfo path, Collection<FilterableCase> cases) {
		Function<FilterableCase, Boolean> validCaseForAction;
		ChronoZonedDateTime<?> now = ZonedDateTime.now();
		switch (request.getUpdateAction()) {
			case BEGIN_SNOOZE:
				validCaseForAction = c -> c.getSnoozeEnd() == null || c.getSnoozeEnd().isBefore(now);
				if (request.getDuration() <= 0 || !request.getSnoozeReason().isPresent() ) {
					throw new IllegalArgumentException("Snooze reason and positive duration are required for batch snooze");
				}
				break;
			case ATTACH:
				if (request.getAttachments().isEmpty()) {
					throw new IllegalArgumentException("No attachments provided for bulk attachment request");
				}
				// fallthrough
			case END_SNOOZE:
				validCaseForAction = c -> c.getSnoozeEnd() != null && c.getSnoozeEnd().isAfter(now);
				break;
			default:
				throw new IllegalArgumentException("Unsupported batch action " + request.getUpdateAction());
		}

		BatchUpdateRequestErrors errors = new BatchUpdateRequestErrors();
		Map<String, FilterableCase> mapped = cases.stream().collect(
				Collectors.toMap(TroubleCaseFixedData::getReceiptNumber, Function.identity()));
		for (String requested : request.getReceiptNumbers()) {
			FilterableCase found = mapped.get(requested);
			if (found == null) {
				errors.missingReceipt(requested);
			} else if (found.getCaseType().getInternalId() != path.getCaseTypeId()) {
				errors.wrongCaseTypeReceipt(requested);
			} else if (!validCaseForAction.apply(found)) {
				errors.ineligibleReceipt(requested);
			}
		}

		if (errors.hasErrors()) {
			throw new BatchUpdateRequestException(errors);
		}
	}

}
