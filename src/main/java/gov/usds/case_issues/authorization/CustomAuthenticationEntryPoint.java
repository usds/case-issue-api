package gov.usds.case_issues.authorization;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final Logger LOG = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {
			String loginURL = "/oauth2/authorization/uscis-icam";
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			JSONObject body = new JSONObject();
			try {
				body.put("LoginURL", loginURL);
			} catch(JSONException e) {
				LOG.error("Unable to parse body  into JSON ", e);
			}
			PrintWriter writer = response.getWriter();
			writer.write(body.toString());
			writer.flush();
	}
}
