package gov.usds.case_issues.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.model.UserInformation;
import gov.usds.case_issues.db.repositories.UserInformationRepository;
import gov.usds.case_issues.model.AttachmentRequest;

@WithMockUser(username = CaseDetailsApiControllerTest.USER_ID, authorities = {"READ_CASES", "UPDATE_CASES"})
public class CaseDetailsApiControllerTest extends ControllerTestBase {

	static final String USER_ID = "d15f7835-7fe7-438d-b889-90a5f5974ec2";
	private static final String CASE_DETAILS_URL_TEMPLATE = "/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}";
	private static final String ACTIVE_SNOOZE_TEMPLATE = CASE_DETAILS_URL_TEMPLATE + "/activeSnooze";

	private static final String VALID_SYS = "C1";
	private static final String SAMPLE_CASE = "BH90210";
	private static final String SAMPLE_CASE_DETAIL_JSON = "{\"receiptNumber\": \"" + SAMPLE_CASE + "\", \"snoozes\": []}";

	private CaseManagementSystem _sys;
	@Autowired
	private UserInformationRepository _userRepo;

	@Before
	public void resetDb() {
		truncateDb();
		_sys = _dataService.ensureCaseManagementSystemInitialized(VALID_SYS, "Cases", "managed");
	}

	@Test
	public void getDetails_pathErrors_notFound() throws Exception {
		initSampleCase();
		this._mvc.perform(detailsRequest("NOPE", "NOPE"))
			.andExpect(status().isNotFound());
		this._mvc.perform(detailsRequest(VALID_SYS, "NOPE"))
			.andExpect(status().isNotFound());
	}

	@Test
	public void getDetails_okCase_expectedResult() throws Exception {
		initSampleCase();
		this._mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json(SAMPLE_CASE_DETAIL_JSON))
			;
	}

	@Test
	public void getDetails_okCaseOkOrigin_okResult()  throws Exception {
		initSampleCase();
		perform(detailsRequest(VALID_SYS, SAMPLE_CASE).header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().isOk())
			.andExpect(content().json(SAMPLE_CASE_DETAIL_JSON))
			;
	}

	@Test
	public void getDetails_okCaseBadOrigin_forbidden()  throws Exception {
		initSampleCase();
		perform(detailsRequest(VALID_SYS, SAMPLE_CASE).header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden())
			;
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
		initSampleCase();
		String tomorrow = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS).withHour(3).toString();
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
	public void snoozeOperations_validCaseNoCsrf_forbidden() throws Exception {
		TroubleCase tc = initSampleCase();
		_dataService.snoozeCase(tc);
		perform(updateSnoozeNoCsrf(VALID_SYS, SAMPLE_CASE, "EVIL", 1, null))
			.andExpect(status().isForbidden());
		perform(endSnoozeNoCsrf(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isForbidden());
		perform(addNoteNoCsrf(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "What up?")))
			.andExpect(status().isForbidden());
	}

	@Test
	public void snoozeOperations_validCaseOkOrigin_ok() throws Exception {
		TroubleCase tc = initSampleCase();
		_dataService.snoozeCase(tc);
		perform(updateSnooze(VALID_SYS, SAMPLE_CASE, "EVIL", 1, null).header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().isOk());
		// add the note *then* do the deletion, plzkthx
		perform(addNote(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "What up?")).header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().is2xxSuccessful());
		perform(endSnooze(VALID_SYS, SAMPLE_CASE).header("Origin", ORIGIN_HTTPS_OK))
			.andExpect(status().isOk());
	}

	@Test
	public void snoozeOperations_validCaseBadOrigin_forbidden() throws Exception {
		TroubleCase tc = initSampleCase();
		_dataService.snoozeCase(tc);
		perform(updateSnooze(VALID_SYS, SAMPLE_CASE, "EVIL", 1, null).header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden());
		perform(endSnooze(VALID_SYS, SAMPLE_CASE).header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden());
		perform(addNote(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "What up?")).header("Origin", ORIGIN_NOT_OK))
			.andExpect(status().isForbidden());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeWithAttachment_technicalIssue_notesStored() throws Exception {
		initSampleCase();
		_dataService.ensureAttachmentSubtypeInitialized("noteSubtypeTag", "Asignee", AttachmentType.TAG, null);

		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(updateSnooze(
				VALID_SYS, SAMPLE_CASE, "assigned_case", 5, null,
				new AttachmentRequest(AttachmentType.TAG, "assignee", "noteSubtypeTag")
			))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"notes\": [{\"content\": \"assignee\"}]}"))
			;
		_mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"notes\": [{\"content\": \"assignee\"}]}"))
			;
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeWithNotes_validCase_notesStored() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(
				updateSnooze(VALID_SYS, SAMPLE_CASE, "Meh", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null))
			)
			.andExpect(status().isOk());
		_mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"notes\": [{\"content\": \"Hello World\"}]}"))
			;
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeWithNotes_noUserRecord_userNameEmpty() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(
				updateSnooze(VALID_SYS, SAMPLE_CASE, "Meh", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null))
			)
			.andExpect(status().isOk());
		UserInformation user = _userRepo.findByUserId(USER_ID);
		_userRepo.delete(user);
		_mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"snoozes\": [{\"user\": {\"id\": \"d15f7835-7fe7-438d-b889-90a5f5974ec2\", \"name\": \"\"}}]}"))
			.andExpect(content().json("{\"notes\": [{\"user\": {\"id\": \"d15f7835-7fe7-438d-b889-90a5f5974ec2\", \"name\": \"\"}}]}"))
			;
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeWithNotes_validCase_containsUserName() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(
				updateSnooze(VALID_SYS, SAMPLE_CASE, "Meh", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null))
			)
			.andExpect(status().isOk());
		_mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json("{\"snoozes\": [{\"user\": {\"id\": \"d15f7835-7fe7-438d-b889-90a5f5974ec2\", \"name\": \"Admin Anna\"}}]}"))
			.andExpect(content().json("{\"notes\": [{\"user\": {\"id\": \"d15f7835-7fe7-438d-b889-90a5f5974ec2\", \"name\": \"Admin Anna\"}}]}"))
			;
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snoozeWithNotes_newLineInContent_notesStored() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isNoContent());
		_mvc.perform(
				updateSnooze(VALID_SYS, SAMPLE_CASE, "Meh", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "Hello\\nWorld", null))
			)
			.andExpect(status().isOk());
		_mvc.perform(detailsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.notes[0].content").value("Hello\\nWorld"))
			;
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snooze_reasonScriptTag_badRequest() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE)).andExpect(status().isNoContent());
		_mvc.perform(
			updateSnooze(VALID_SYS, SAMPLE_CASE, "<script>", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null))
		).andExpect(status().isBadRequest());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snooze_invalidAttachmentType_badRequest() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE)).andExpect(status().isNoContent());
		JSONObject body = new JSONObject()
			.put("reason", "reason")
			.put("duration", 1);
		JSONArray notesArray = new JSONArray();
		JSONObject noteJson = new JSONObject();
		noteJson.put("type", "invalid atttachment type");
		noteJson.put("content", "hello world");
		notesArray.put(noteJson);
		body.put("notes", notesArray);
		_mvc.perform(
			put("/api/caseDetails/{caseManagementSystemTag}/{receiptNumber}/activeSnooze", VALID_SYS, SAMPLE_CASE)
				.contentType("application/json")
				.content(body.toString())
				.with(csrf())
			)
			.andExpect(status().isBadRequest());
	}

	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snooze_attachmentContentScriptTag_badRequest() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE)).andExpect(status().isNoContent());
		_mvc.perform(
			updateSnooze(VALID_SYS, SAMPLE_CASE, "reason", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "<script></script>", null))
		).andExpect(status().isBadRequest());
	}

	// this might be me failing for something other than the script tag
	@Test
	@SuppressWarnings("checkstyle:MagicNumber")
	public void snooze_attachmentSubtypeScriptTag_badRequest() throws Exception {
		initSampleCase();
		_mvc.perform(getSnooze(VALID_SYS, SAMPLE_CASE)).andExpect(status().isNoContent());
		_mvc.perform(
			updateSnooze(VALID_SYS, SAMPLE_CASE, "reason", 1, null, new AttachmentRequest(AttachmentType.COMMENT, "", "<script></script>"))
		).andExpect(status().isBadRequest());
	}

	@Test
	public void addNoteToSnooze_snoozedCase_notesStored() throws Exception {
		TroubleCase troubleCase = initSampleCase();
		_dataService.snoozeCase(troubleCase);
		_mvc.perform(addNote(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null)))
			.andExpect(status().isCreated())
			.andExpect(content().json(attachmentJson(AttachmentType.COMMENT, null, "Hello World").toString()))
			.andExpect(jsonPath("$.id", Matchers.greaterThan(1)))
			;
	}

	@Test
	public void addAttachmentToSnooze_snoozedCaseWithAttachment_bothStored() throws Exception {
		TroubleCase troubleCase = initSampleCase();
		JSONObject attachment = attachmentJson(AttachmentType.COMMENT, null, "We're back");
		JSONArray ar = new JSONArray(new JSONObject[] {
				attachmentJson(AttachmentType.COMMENT, null, "Hello World"),
				attachment
		});
		_dataService.snoozeCase(troubleCase);
		_mvc.perform(addNote(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null)))
			.andExpect(status().isCreated());
		_mvc.perform(addNote(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "We're back", null)))
			.andExpect(status().isCreated())
			.andExpect(content().json(attachment.toString()))
			;
		_mvc.perform(attachmentsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json(ar.toString()))
			.andExpect(jsonPath("$[0].id", Matchers.greaterThan(1)))
			.andExpect(jsonPath("$[1].id", Matchers.greaterThan(1)))
			;
	}

	@Test
	public void addAttachmentToSnooze_duplicateAttachment_conflict() throws Exception {
		TroubleCase troubleCase = initSampleCase();
		JSONObject attachment = attachmentJson(AttachmentType.COMMENT, null, "Hello World");
		JSONArray ar = new JSONArray(new JSONObject[] {attachment});
		_dataService.snoozeCase(troubleCase);
		AttachmentRequest req = new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null);
		_mvc.perform(addNote(VALID_SYS, SAMPLE_CASE, req))
			.andExpect(status().isCreated())
			.andExpect(content().json(attachment.toString()))
			;
		_mvc.perform(addNote(VALID_SYS, SAMPLE_CASE, req))
			.andExpect(status().isConflict())
			;
		_mvc.perform(attachmentsRequest(VALID_SYS, SAMPLE_CASE))
			.andExpect(status().isOk())
			.andExpect(content().json(ar.toString()))
			;
	}

	@Test
	public void addNoteToSnooze_activeCase_badRequest() throws Exception {
		initSampleCase();
		_mvc.perform(addNote(VALID_SYS, SAMPLE_CASE, new AttachmentRequest(AttachmentType.COMMENT, "Hello World", null)))
			.andExpect(status().isBadRequest());
	}

	private TroubleCase initSampleCase() {
		CaseType type = _dataService.ensureCaseTypeInitialized("T2", "Ahnold", "Metal and scary");
		TroubleCase troubleCase = _dataService.initCase(_sys, SAMPLE_CASE, type, ZonedDateTime.now());

		String createdBy = troubleCase.getCreatedBy();
		UserInformation user = new UserInformation(createdBy, "Admin Anna");
		_userRepo.save(user);

		return troubleCase;
	}

	private MockHttpServletRequestBuilder detailsRequest(String systemTag, String receipt) {
		return get(CASE_DETAILS_URL_TEMPLATE, systemTag, receipt);
	}

	private MockHttpServletRequestBuilder attachmentsRequest(String systemTag, String receipt) {
		return get(CASE_DETAILS_URL_TEMPLATE + "/attachments", systemTag, receipt);
	}

	private MockHttpServletRequestBuilder getSnooze(String systemTag, String receipt) {
		return get(ACTIVE_SNOOZE_TEMPLATE, systemTag, receipt);
	}

	private MockHttpServletRequestBuilder endSnooze(String systemTag, String receipt) {
		return endSnoozeNoCsrf(systemTag, receipt).with(csrf());
	}

	private MockHttpServletRequestBuilder endSnoozeNoCsrf(String systemTag, String receipt) {
		return delete(ACTIVE_SNOOZE_TEMPLATE, systemTag, receipt);
	}


	private MockHttpServletRequestBuilder updateSnooze(String systemTag, String receipt, String reason, int duration, String details,
			AttachmentRequest... notes) throws JSONException {
		return updateSnoozeNoCsrf(systemTag, receipt, reason, duration, details, notes).with(csrf());
	}

	private MockHttpServletRequestBuilder updateSnoozeNoCsrf(String systemTag, String receipt, String reason, int duration, String details,
			AttachmentRequest... notes)
			throws JSONException {
		JSONObject body = new JSONObject()
			.put("reason", reason)
			.put("details", details)
			.put("duration", duration);
		if (notes != null && notes.length > 0) {
			JSONArray notesArray = new JSONArray();
			for (AttachmentRequest req : notes) {
				JSONObject noteJson = new JSONObject();
				noteJson.put("type", req.getNoteType().name());
				noteJson.put("content", req.getContent());
				noteJson.put("subtype", req.getSubtype());
				notesArray.put(noteJson);
			}
			body.put("notes", notesArray);
		}
		return put(ACTIVE_SNOOZE_TEMPLATE, systemTag, receipt)
			.contentType("application/json")
			.content(body.toString())
			;
	}

	private MockHttpServletRequestBuilder addNote(String systemTag, String receipt, AttachmentRequest note)
			throws JSONException {
		return addNoteNoCsrf(systemTag, receipt, note).with(csrf());
	}

	private MockHttpServletRequestBuilder addNoteNoCsrf(String systemTag, String receipt, AttachmentRequest note)
			throws JSONException {
		JSONObject body = new JSONObject();
		body.put("type", note.getNoteType().name());
		body.put("content", note.getContent());
		body.put("subtype", note.getSubtype());
		return post(ACTIVE_SNOOZE_TEMPLATE + "/attachments", systemTag, receipt)
			.contentType("application/json")
			.content(body.toString())
			;
	}

	private static JSONObject attachmentJson(AttachmentType type, String subtype, String content) {
		JSONObject j = new JSONObject();
		j.put("type", type.name());
		j.put("content", content);
		if (subtype != null) {
			j.put("subtype", subtype);
		}
		return j;
	}
}
