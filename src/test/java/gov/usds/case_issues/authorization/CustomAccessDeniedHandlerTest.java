package gov.usds.case_issues.authorization;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

public class CustomAccessDeniedHandlerTest {

	@Test
	public void handle_called_ResopnseForbiden() throws IOException, ServletException {
		CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		AccessDeniedException exc = mock(AccessDeniedException.class);
		handler.handle(request, response, exc);
		verify(response).sendError(HttpStatus.FORBIDDEN.value());
	}
}
