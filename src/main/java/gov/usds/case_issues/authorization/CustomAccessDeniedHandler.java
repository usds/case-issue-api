package gov.usds.case_issues.authorization;

import java.io.IOException;
import java.time.ZonedDateTime;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;


public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	private static final Logger LOG = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException exc) throws IOException, ServletException {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				JSONObject message = new JSONObject();
				try {
					message.put("User", auth.getName());
					message.put("RequestURI", request.getRequestURI());
					message.put("Details", auth.getDetails());
					message.put("Date", ZonedDateTime.now());
					message.put("StatusCode", HttpStatus.FORBIDDEN.value());
					LOG.warn(message.toString());
				} catch(JSONException e) {
					LOG.error("Unable to parse log event into JSON ", e);
				}
			}
			response.sendError(HttpStatus.FORBIDDEN.value());
	}
}
