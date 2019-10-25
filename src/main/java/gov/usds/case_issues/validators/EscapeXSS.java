package gov.usds.case_issues.validators;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

/**
 * A custom constraint for escaping script tags and other strings containing potential XSS attacks.
 */
@Constraint(validatedBy = { })
/*
 * ((   ).|\\s)* Warps expressions to allow 0 more of any character (including line breaks)
 * Wrapped expressions use ?! to perform a negative lookahead and define expression that cannot exist
 *   (?!(\\b)(on\\S+)(\\s*)=) "on(string | whitespace) =" to catch onerror= or onclick= html attributes
 *   (?!javascript)           the string literal "javascript"
 *   (?!(<(\\s*)script))      the opening or closing of a script tag "<script" or "</script"
 */
@Pattern(regexp="((?!(\\b)(on\\S+)(\\s*)=)(?!javascript)(?!(<(\\s*)script)).|\\s)*")
@Documented
@Retention(RUNTIME)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
public @interface EscapeXSS {
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
    String message() default "Parameter rejected because it contains untrusted symbols";
}
