package gov.usds.case_issues.controllers.errors;

import java.time.ZonedDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;

/**
 * Trivial wrapper for error messages, to match the standard Spring JSON error format.
 <pre>
   {
     "timestamp": "2019-07-11T18:28:59.707+0000",
     "status": 404,
     "error": "Not Found",
     "message": "No message available",
     "path": "/rest/profile/troubleCases"
   }
</pre>
*/
public class SpringRestError {

	private ZonedDateTime timestamp;
	private HttpStatus status;
	private Throwable throwable;
	private HttpServletRequest request;

	public SpringRestError(Throwable t, HttpStatus s, HttpServletRequest req) {
		timestamp = ZonedDateTime.now();
		status = s;
		throwable = t;
		request = req;
	}

	public ZonedDateTime getTimestamp() {
		return timestamp;
	}

	public int getStatus() {
		return status.value();
	}

	public String getError() {
		return status.getReasonPhrase();
	}

	public String getMessage() {
		return throwable.getMessage();
	}

	public String getPath() {
		return request.getRequestURI();
	}
}
