package gov.usds.case_issues.services;

import java.util.List;
import java.util.Optional;

import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.validators.TagFragment;

/**
 * Service interface for fetching read-only case lists.
 */
public interface CasePagingService {

	List<? extends CaseSummary> getActiveCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			int size
	);

	default List<? extends CaseSummary> getActiveCases(
			@TagFragment String system,
			@TagFragment String caseType,
			@TagFragment String firstReceipt,
			int size) {
		return getActiveCases(system, caseType, firstReceipt, null, size);
	}
	List<? extends CaseSummary> getSnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			Optional<String> snoozeReason,
			int size
	);

	default List<? extends CaseSummary> getSnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			int size) {
		return getSnoozedCases(caseManagementSystemTag, caseTypeTag, receiptNumber, null, Optional.empty(), size);
	}

	List<? extends CaseSummary> getPreviouslySnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			DateRange caseCreationRange,
			int size
	);

	default List<? extends CaseSummary> getPreviouslySnoozedCases(
			@TagFragment String caseManagementSystemTag,
			@TagFragment String caseTypeTag,
			@TagFragment String receiptNumber,
			int size) {
		return getPreviouslySnoozedCases(caseManagementSystemTag, caseTypeTag, receiptNumber, null, size);
	}


}