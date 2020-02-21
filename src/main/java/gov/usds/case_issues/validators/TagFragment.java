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
import javax.validation.constraints.Pattern;

/**
 * A custom constraint for "one word" validations, to avoid repeating (or nearly repeating)
 * the same {@link Pattern} annotation in a million places.
 * <ul>
 * <li>\\w* matches 0 or more word chars [A-Z, a-z, 0-9, _]
 * <li>[-\\w]* match 0 or more words seperated by [-]
 * </ul>
 * Examples:
 *   Valid: "1c58a9ab-1f8c-4743-b067-f55c60c22080", "abc_XYZ_123"
 *   Invalid: "\n\r", "\s", "?"
 */
@Constraint(validatedBy = { })
@Pattern(regexp="\\w*[-\\w]*")
@Documented
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER, LOCAL_VARIABLE })
public @interface TagFragment {
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
    String message() default "Parameter must be a single word or word-like value";
}
