package gov.usds.case_issues.model;

import java.time.ZonedDateTime;
import java.util.Map;

public interface CaseRequest {

	String getReceiptNumber();

	ZonedDateTime getCaseCreation();

	Map<String, Object> getExtraData();

}