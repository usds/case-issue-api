package gov.usds.case_issues.validators;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.hibernate.validator.constraints.Length;

/**
 * Constraint to enforce that a string is non-empty and no longer than the maximum allowed by
 * our default string column length in the database (currently a <code>varchar(255)</code>).
 */
@Constraint(validatedBy = { })
@Length(min=1, max=PersistedId.MAX_ID_LENGTH)
@Documented
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER, LOCAL_VARIABLE })
public @interface PersistedId {

	public static final int MAX_ID_LENGTH = 255;

	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
	String message() default "Parameter must be a string of " + MAX_ID_LENGTH + " characters or less";
}
