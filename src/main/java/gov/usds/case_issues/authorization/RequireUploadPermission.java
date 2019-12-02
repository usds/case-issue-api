package gov.usds.case_issues.authorization;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Authorization annotation to specify that this method is only available to users with the
 * permission to upload new issue lists to the system ({@link CaseIssuePermission#UPDATE_ISSUES}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission)"
		+ ".UPDATE_ISSUES.name())")
public @interface RequireUploadPermission {

}
