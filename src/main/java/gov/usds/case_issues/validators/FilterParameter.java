package gov.usds.case_issues.validators;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

/**
 * Constraint to validate parameter names for HTTP endpoints with variable query-string parameters.
 */
@Constraint(validatedBy = { })
@Pattern(regexp="\\w+(?:\\[\\w+\\])*")
@Documented
@Retention(RUNTIME)
@Target({ TYPE_USE })
public @interface FilterParameter {
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
    String message() default "Parameter names must be simple strings or dictionary entries";
}
