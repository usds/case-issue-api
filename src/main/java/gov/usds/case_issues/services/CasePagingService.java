package gov.usds.case_issues.services;

import java.util.Optional;

import gov.usds.case_issues.model.CaseListResponse;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.validators.TagFragment;

/**
 * Service interface for fetching read-only case lists.
 */
public interface CasePagingService {

	CaseListResponse getActiveCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			int size
	);

	default CaseListResponse getActiveCases(
			@TagFragment String system,
			@TagFragment String caseType,
			@TagFragment String firstReceipt,
			int size) {
		return getActiveCases(system, caseType, firstReceipt, null, size);
	}
	CaseListResponse getSnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			Optional<String> snoozeReason,
			int size
	);

	default CaseListResponse getSnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			int size) {
		return getSnoozedCases(caseManagementSystemTag, caseTypeTag, receiptNumber, null, Optional.empty(), size);
	}

	CaseListResponse getPreviouslySnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			int size
	);

	default CaseListResponse getPreviouslySnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			int size) {
		return getPreviouslySnoozedCases(caseManagementSystemTag, caseTypeTag, receiptNumber, null, size);
	}


}
