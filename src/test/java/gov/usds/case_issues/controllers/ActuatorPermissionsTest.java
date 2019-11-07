package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;

@WithAnonymousUser
public class ActuatorPermissionsTest extends ControllerTestBase {

	@Value("${management.endpoints.web.base-path:/actuator}")
	private String _basePath;

	@Test
	public void getHealth_anonymous_forbidden() throws Exception {
		doActuatorGet("health").andExpect(status().isForbidden())
		;
	}

	@Test
	@WithMockUser(authorities="UPDATE_STRUCTURE") // a highly-privileged but not devops user
	public void getHealth_contentAdmin_forbidden() throws Exception {
		doActuatorGet("health").andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities="MANAGE_APPLICATION")
	public void getHealth_systemAdmin_fullResult() throws Exception {
		doActuatorGet("health")
			.andExpect(status().isOk())
			.andExpect(content().json("{\"status\": \"UP\", \"details\": {\"db\": {\"status\": \"UP\"}}}", false))
		;
	}

	@Test
	public void getInfo_anonymous_fullResult() throws Exception {
		doActuatorGet("info")
			.andExpect(status().isOk())
			// if run in the IDE, this does not pick up properties
			// in any case we do not need to test the functioning of library code
			.andExpect(content().json("{}", false))
		;
	}

	@Test
	public void getLoggers_anonymous_forbidden() throws Exception {
		doActuatorGet("loggers").andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(authorities="MANAGE_APPLICATION")
	public void getLoggers_systemAdmin_ok() throws Exception {
		doActuatorGet("loggers").andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities="UPDATE_STRUCTURE") // a highly-privileged but not devops user
	public void getLoggers_contentAdmin_forbidden() throws Exception {
		doActuatorGet("loggers").andExpect(status().isForbidden());
	}

	private ResultActions doActuatorGet(String id) throws Exception {
		return perform(get(_basePath + "/{id}", id));
	}
}
