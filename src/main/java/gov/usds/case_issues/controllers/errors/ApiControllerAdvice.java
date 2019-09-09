package gov.usds.case_issues.controllers.errors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import gov.usds.case_issues.model.ApiModelNotFoundException;

@RestControllerAdvice("gov.usds.case_issues.controllers") // maybe make this type safe?
public class ApiControllerAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(ApiControllerAdvice.class);

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public SpringRestError handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest req) {
		LOG.debug("Illegal argument exception raised: {}", e.getMessage());
		return new SpringRestError(e, HttpStatus.BAD_REQUEST, req);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public SpringRestError handleInvalidApiRequest(ApiModelNotFoundException e, HttpServletRequest req) {
		LOG.debug("Not found error for {}/{}", e.getEntityType(), e.getEntityId());
		return new SpringRestError(e, HttpStatus.NOT_FOUND, req);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public SpringRestError handleConstraintViolation(ConstraintViolationException e, HttpServletRequest req) {
		LOG.warn("Got constraint violation", e);
		return new SpringRestError(e, HttpStatus.BAD_REQUEST, req);
	}
}
