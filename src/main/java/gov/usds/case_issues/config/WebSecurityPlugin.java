package gov.usds.case_issues.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * An interface that is like Consumer, but is not composable (because I don't need it to be),
 * and is declared to throw an exception (because that seems strictly better than converting all the
 * underlying Exceptions to RuntimeExceptions).
 */
@FunctionalInterface
public interface WebSecurityPlugin {

	void apply(HttpSecurity t) throws Exception;

}
