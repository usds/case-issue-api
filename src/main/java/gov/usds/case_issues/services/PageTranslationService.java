package gov.usds.case_issues.services;

import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;
import gov.usds.case_issues.services.CaseListService.CasePageInfo;
import gov.usds.case_issues.validators.TagFragment;

public interface PageTranslationService {

	CaseGroupInfo translatePath(@TagFragment String caseManagementSystemTag, @TagFragment String caseTypeTag);

	CasePageInfo translatePath(@TagFragment String caseManagementSystemTag, @TagFragment String caseTypeTag, @TagFragment String receipt);

}