package gov.usds.case_issues.model;

/**
 * Exception to convey to the API consumer that a constraint other than a database constraint
 * (i.e. a business rule implemented in Java) was violated. Should produce a 409 Conflict status,
 * rather than a 400 Bad Request. 
 */
public class BusinessConstraintViolationException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public BusinessConstraintViolationException(String message) {
		super(message);
	}
}
