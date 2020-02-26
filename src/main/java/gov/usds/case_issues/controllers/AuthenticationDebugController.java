package gov.usds.case_issues.controllers;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

/**
 * Development-only controller to see information about the current user, to validate that
 * authentication and authorization are at least sort of working.
 */
@RestController
@RequestMapping("/auth-info")
@Profile("auth-testing")
@ApiIgnore
public class AuthenticationDebugController {

	@GetMapping
	public Authentication getUser(Authentication u) {
		return u;
	}

	@GetMapping("details")
	public Map<String, ?> getDetails(Authentication auth, HttpServletRequest req) {
		Map<String, Object> map = new HashMap<>();
		map.put("authentication", auth);
		map.put("authType", req.getAuthType());
		map.put("requestPrincipal", req.getUserPrincipal());
		HttpSession session = req.getSession(false);
		map.put("sessionId", session == null ? null : session.getId());
		return map;
	}

	@GetMapping("types")
	public Map<String, ?> getTypes(Authentication auth, HttpServletRequest req) {
		Map<String, Object> map = new HashMap<>();
		map.put("authenticationClass", auth.getClass());
		Object principal = auth.getPrincipal();
		map.put("authPrincipalClass", principal.getClass());
		try {
			map.put("authNestedPrincipalClass", getSubPrincipal(principal).getClass());
		} catch (SecurityException | ReflectiveOperationException | IllegalArgumentException  e) {
			map.put("authNestedPrincipalExceptionClass", e.getClass());
		}
		Principal userPrincipal = req.getUserPrincipal();
		map.put("requestPrincipalClass", userPrincipal.getClass());
		try {
			map.put("requestNestedPrincipalClass", getSubPrincipal(userPrincipal).getClass());
		} catch (SecurityException | ReflectiveOperationException | IllegalArgumentException  e) {
			map.put("requestNestedPrincipalExceptionClass", e.getClass());
		}
		HttpSession session = req.getSession(false);
		map.put("sessionClass", session != null ? session.getClass() : null);
		return map;

	}

	private Object getSubPrincipal(Object principal) throws ReflectiveOperationException, IllegalArgumentException  {
		Method m = principal.getClass().getMethod("getPrincipal");
		return m.invoke(principal);
	}

}
