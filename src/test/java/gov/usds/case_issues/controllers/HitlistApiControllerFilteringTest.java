package gov.usds.case_issues.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.test_util.CaseListFixtureService;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureAttachment;
import gov.usds.case_issues.test_util.CaseListFixtureService.FixtureCase;
import gov.usds.case_issues.test_util.CaseListFixtureService.Keywords;

@WithMockUser(authorities="READ_CASES")
public class HitlistApiControllerFilteringTest extends ControllerTestBase {

	private static final int DEFAULT_PAGE_SIZE = 3;
	private static final String PARITY_FILTER = "filter_dataField[parity]";
	private static final String ANY_LINK = "filter_hasLinkType";
	private static final String TROUBLE_LINK = "filter_hasLink[trouble]";
	@Autowired
	private CaseListFixtureService _fixtures;

	@Before
	public void reset() {
		_fixtures.initFixtures();
	}

	@Test
	public void snoozedCases_noFilter_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED01))
		;
	}

	@Test
	public void snoozedCases_withTicket_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(ANY_LINK, "trouble");
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED02, FixtureCase.SNOOZED04, FixtureCase.SNOOZED03))
		;
	}

	@Test
	public void alarmedCases_withTicket_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(ANY_LINK, "trouble");
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.DESNOOZED01))
		;
	}

	@Test
	public void alarmedCases_withSpecificGoodTicket_oneCase() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(TROUBLE_LINK, FixtureAttachment.LINK01.name());
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.DESNOOZED01))
		;
	}

	@Test
	public void alarmedCases_withSpecificBadTicket_emptyList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(TROUBLE_LINK, FixtureAttachment.LINK02.name());
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(emptyJsonList())
		;
	}

	@Test
	public void snoozedCases_withColorTag_oneResult() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add("filter_hasTagType", "color");
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED02))
		;
	}

	@Test
	public void snoozedCases_withNonexistentTicket_emptyList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(TROUBLE_LINK, "NOPE");
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(emptyJsonList());
	}

	@Test
	public void snoozedCases_withLinkTypeNameSwitch_emptyList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(TROUBLE_LINK, FixtureAttachment.LINKEXT1.name());
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(emptyJsonList());
	}

	@Test
	public void snoozedCases_withTagTicketNameSwitch_emptyList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(TROUBLE_LINK, FixtureAttachment.TAG_BLUE.name());
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(emptyJsonList());
	}

	@Test
	public void snoozedCases_withTagTicketTypeSwitch_emptyList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add("filter_hasLink[color]", FixtureAttachment.TAG_BLUE.name());
		doGetCases(CaseSnoozeFilter.ALARMED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(emptyJsonList());
	}

	@Test
	public void snoozedCases_withComment_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add("filter_hasAnyComment", "true");
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED04))
		;
	}

	@Test
	public void snoozedCases_withSpecificComment_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add("filter_hasComment", FixtureAttachment.COMMENT2.name());
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED05, FixtureCase.SNOOZED04))
		;
	}

	@Test
	public void snoozedCases_dataParityEven_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(PARITY_FILTER, Keywords.EVEN);
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED05, FixtureCase.SNOOZED02, FixtureCase.SNOOZED03))
		;
	}

	@Test
	public void uncheckedCases_dataParityEven_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(PARITY_FILTER, Keywords.EVEN);
		doGetCases(CaseSnoozeFilter.UNCHECKED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.ACTIVE03, FixtureCase.ACTIVE05))
		;
	}
	@Test
	public void snoozedCases_dataParityEvenLaterPage_correctList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add(PARITY_FILTER, Keywords.EVEN);
		additional.add("pageReference", FixtureCase.SNOOZED05.name());
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(caseJson(FixtureCase.SNOOZED02, FixtureCase.SNOOZED03))
		;
	}

	@Test
	public void snoozedCases_dataUnknownField_emptyList() throws Exception {
		MultiValueMap<String, String> additional = new LinkedMultiValueMap<>();
		additional.add("filter_dataField[fred]", Keywords.EVEN);
		doGetCases(CaseSnoozeFilter.SNOOZED, DEFAULT_PAGE_SIZE, additional)
			.andExpect(status().isOk())
			.andExpect(emptyJsonList())
		;
	}

	private static ResultMatcher emptyJsonList() {
		return content().json("[]", true);
	}

	private ResultMatcher caseJson(FixtureCase... cases) {
		return jsonPath("$[*].receiptNumber").value(Stream.of(cases).map(c -> c.name()).collect(Collectors.toList()));
	}

	private ResultActions doGetCases(CaseSnoozeFilter main, int pageSize, MultiValueMap<String, String> additional) throws Exception {
		return doGetCases(Collections.singleton(main), pageSize, additional);
	}

	private ResultActions doGetCases(Set<CaseSnoozeFilter> main, int pageSize, MultiValueMap<String, String> additional) throws Exception {
		MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
		baseParams.put("mainFilter", main.stream().map(CaseSnoozeFilter::name).collect(Collectors.toList()));
		baseParams.add("size", Integer.toString(pageSize));
		MockHttpServletRequestBuilder req = get(HitlistApiControllerTest.API_PATH, CaseListFixtureService.SYSTEM, CaseListFixtureService.CASE_TYPE)
			.params(baseParams)
			.params(additional)
			;
		return perform(req);
	}

}
