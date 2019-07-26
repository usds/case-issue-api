package gov.usds.case_issues.controllers.errors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import gov.usds.case_issues.model.ApiModelNotFoundException;

@RestControllerAdvice("gov.usds.case_issues.controllers") // maybe make this type safe?
public class ApiControllerAdvice {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public SpringRestError handleInvalidApiRequest(ApiModelNotFoundException e, HttpServletRequest req) {
		return new SpringRestError(e, HttpStatus.NOT_FOUND, req);
	}
}
