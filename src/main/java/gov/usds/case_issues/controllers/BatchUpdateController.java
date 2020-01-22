package gov.usds.case_issues.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.authorization.RequireUpdateCasePermission;
import gov.usds.case_issues.controllers.errors.SpringRestError;
import gov.usds.case_issues.db.model.BatchUpdateRequestErrors;
import gov.usds.case_issues.model.BatchUpdateRequest;
import gov.usds.case_issues.model.BatchUpdateRequestException;
import gov.usds.case_issues.services.BatchUpdateService;

@RestController
@RequireUpdateCasePermission
@RequestMapping("/api/batch/{caseManagementSystemTag}/{caseTypeTag}/")
@Validated
public class BatchUpdateController {

	@Autowired
	private BatchUpdateService _batchService;

	@PostMapping
	public void doBatchActions(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag,
			@RequestBody BatchUpdateRequest updateAction) {
		_batchService.processBatchAction(caseManagementSystemTag, caseTypeTag, updateAction);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public SpringRestError handleBatchErrors(BatchUpdateRequestException e, HttpServletRequest req) {
		return new BatchError(e, HttpStatus.BAD_REQUEST, req, e.getErrors());
	}

	public static class BatchError extends SpringRestError {

		private BatchUpdateRequestErrors details;

		public BatchError(Throwable t, HttpStatus s, HttpServletRequest req, BatchUpdateRequestErrors details) {
			super(t, s, req);
			this.details = details;
		}

		public BatchUpdateRequestErrors getDetails() {
			return details;
		}
	}
}
