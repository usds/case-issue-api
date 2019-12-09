package gov.usds.case_issues.authorization;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Authorization annotation to specify that this method is only available to users with the
 * permission to update case and snooze information in the system ({@link CaseIssuePermission#UPDATE_CASES}).
 */
@Documented
@Retention(RUNTIME)
@Target({METHOD, TYPE})
@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission)"
		+ ".UPDATE_CASES.name())")
public @interface RequireUpdateCasePermission {

}
