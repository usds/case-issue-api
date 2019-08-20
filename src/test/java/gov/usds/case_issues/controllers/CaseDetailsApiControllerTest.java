package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.NoteType;
import gov.usds.case_issues.model.NoteRequest;

public class CaseDetailsApiControllerTest extends ControllerTestBase {

	private static final String VALID_SYS = "C1";
	private static final String SAMPLE_CASE = "BH90210";

	private CaseManagementSystem _sys;

	@Before
	public void resetDb() {
		truncateDb();
		_sys = _dataService.ensureCaseManagementSystemInitialized(VALID_SYS, "Cases", "managed");
	}

	@Test
	public void getDetails_pathErrors_notFound() throws Exception {
		CaseType type = _dataService.ensureCaseTypeInitialized("T2", "Ahnold", "Metal and scary");
		_dataService.initCase(_sys, SAMPLE_CASE, type, ZonedDateTime.now());

		this._mvc.perform(detailsRequest("NOPE", "NOPE"))
			.andExpect(status().isNotFound());
		this._mvc.perform(detailsRequest(VALID_SYS, "NOPE"))
			.andExpect(status().isNotFound());
		this._mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeOperations_noCase_notFound() throws Exception {
		_mvc.perform(getSnooze(VALID_SYS, "NOSUCHCASE"))
			.andExpect(status().isNotFound());
		_mvc.perform(updateSnooze(VALID_SYS, "NOSUCHCASE", "BECAUSE", 3, "yepyep"))
			.andExpect(status().isNotFound());
		_mvc.perform(endSnooze(VALID_SYS, "NOSUCHCASE"))
			.andExpect(status().isNotFound());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeOperations_validCase_expectedResults() throws Exception {
		CaseType type = _dataService.ensureCaseTypeInitialized("T2", "Ahnold", "Metal and scary");
		String tomorrow = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS).withHour(3).toString();
		_dataService.initCase(_sys, SAMPLE_CASE, type, ZonedDateTime.now());
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(endSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(updateSnooze(VALID_SYS, SAMPLE_CASE, "BECAUSE", 1, null))
			.andExpect(status().isOk())
			.andExpect(jsonPath("snoozeReason").value("BECAUSE")) // THAT IS A TERRIBLE API FAIL
			.andExpect(jsonPath("snoozeEnd").value(Matchers.startsWith(tomorrow)))  // THIS IS NOT GREAT EITHER
			;
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("snoozeReason").value("BECAUSE"))
			.andExpect(jsonPath("snoozeEnd").value(Matchers.startsWith(tomorrow)))
			;
		_mvc.perform(endSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk());
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
	}


	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeWithNotes_validCase_notesStored() throws Exception {
		CaseType type = _dataService.ensureCaseTypeInitialized("T2", "Ahnold", "Metal and scary");
		_dataService.initCase(_sys, SAMPLE_CASE, type, ZonedDateTime.now());
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(updateSnooze(VALID_SYS, SAMPLE_CASE, "Meh", 1, null,
				new NoteRequest(NoteType.COMMENT, "Hello World", null)));
		_mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"notes\": [{\"content\": \"Hello World\"}]}"))
			;
	}

	private MockHttpServletRequestBuilder detailsRequest(String systemTag, String receipt) {
		return get("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}", systemTag, receipt);
	}

	private MockHttpServletRequestBuilder getSnooze(String systemTag, String receipt) {
		return get("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}/activeSnooze", systemTag, receipt);
	}

	private MockHttpServletRequestBuilder endSnooze(String systemTag, String receipt) {
		return delete("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}/activeSnooze", systemTag, receipt);
	}

	private MockHttpServletRequestBuilder updateSnooze(String systemTag, String receipt, String reason, int duration, String details,
			NoteRequest... notes)
			throws JSONException {
		JSONObject body = new JSONObject()
			.put("reason", reason)
			.put("details", details)
			.put("duration", duration);
		if (notes != null && notes.length > 0) {
			JSONArray notesArray = new JSONArray();
			for (NoteRequest req : notes) {
				JSONObject noteJson = new JSONObject();
				noteJson.put("type", req.getNoteType().name());
				noteJson.put("content", req.getContent());
				noteJson.put("subtype", req.getSubtype());
				notesArray.put(noteJson);
			}
			body.put("notes", notesArray);
		}
		return put("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}/activeSnooze", systemTag, receipt)
			.contentType("application/json")
			.content(body.toString())
			;
	}
}
