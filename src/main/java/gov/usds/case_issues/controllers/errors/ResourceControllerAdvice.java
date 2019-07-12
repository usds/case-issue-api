package gov.usds.case_issues.controllers.errors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedRuntimeException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import gov.usds.case_issues.controllers.SpringRestError;

/**
 * ControllerAdvice to detect and appropriately wrap constraint violations and
 * validation failures in CRUD operations provided by spring-data-rest.
 */
@RestControllerAdvice("org.springframework.data.rest.webmvc") // could also use annotations, if preferred
public class ResourceControllerAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceControllerAdvice.class);

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public SpringRestError handleNestedRuntime(NestedRuntimeException e, HttpServletRequest req) {
		LOG.warn("Got NestedRuntimeException", e);
		if (null != e.getRootCause()) {
			LOG.warn("Root cause class is a {}", e.getRootCause().getClass());
			switch(e.getRootCause().getClass().getName()) {
				case "javax.validation.ConstraintViolationException":
					return new SpringRestError(e.getRootCause(), HttpStatus.BAD_REQUEST, req);
				default: /* noop */
			}
		}
		throw e;
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	public SpringRestError handleDataIntegrity(DataIntegrityViolationException e, HttpServletRequest req) {
		LOG.warn("Got a data integrity violation!");
		return new SpringRestError(e, HttpStatus.CONFLICT, req);
	}
}

