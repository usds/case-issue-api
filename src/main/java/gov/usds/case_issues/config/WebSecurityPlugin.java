package gov.usds.case_issues.config;

import java.util.function.Consumer;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public interface WebSecurityPlugin extends Consumer<HttpSecurity> {

}
