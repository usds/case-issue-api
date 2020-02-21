package gov.usds.case_issues.authorization;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Very restrictive: require both the upload permission and the data-modification permission.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
@PreAuthorize(
	  "hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).UPDATE_ISSUES.name())"
	+ " and "
	+ "hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).UPDATE_STRUCTURE.name())"
)
public @interface RequireUploadAndStructurePermission {

}
