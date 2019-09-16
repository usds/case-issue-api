package gov.usds.case_issues.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter that puts a guard around responses to requests that appear to come from a JavaScript
 * client rather than from direct browsing, checks if they appear to be redirecting to a login
 * page, and if changes the response to a simple (probably too simple) 401 Unauthorized.
 *
 */
public class JsonRedirectPreventingFilter implements Filter {

	private static final Logger LOG = LoggerFactory.getLogger(JsonRedirectPreventingFilter.class);

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String accept = request.getHeader("Accept");
		LOG.debug("Handling request for {} with Accept: {}", request.getRequestURI(), accept);
		if (accept != null && accept.contains("text/html")) { // this is probably a browser qua browser, not JS client or curl
			chain.doFilter(servletRequest, servletResponse);
		} else {
			LOG.debug("Wrapping the response to intercept redirects");
			RedirectPreventingResponseWrapper response = new RedirectPreventingResponseWrapper((HttpServletResponse) servletResponse);
			chain.doFilter(servletRequest, response);
		}
	}

	private static class RedirectPreventingResponseWrapper extends HttpServletResponseWrapper {

		public RedirectPreventingResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			LOG.info("Intercepted a redirect on a JSON request: sending 401 instead");
			sendError(SC_UNAUTHORIZED);
		}
	}
}
