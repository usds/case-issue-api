package gov.usds.case_issues.controllers;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.services.CaseListService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/cases/{caseManagementSystemTag}/{caseTypeTag}")
@CrossOrigin("http://localhost:3000")
public class HitlistApiController {

	@Autowired
	private CaseListService _listService;

	@GetMapping("snoozed")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
			value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.", defaultValue = "20"),
	})
	public List<CaseSummary> getSnoozedCases(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @ApiIgnore Pageable pageMe) {
		return _listService.getSnoozedCases(caseManagementSystemTag, caseTypeTag, pageMe);
	}

	@GetMapping("active")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
				value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
		@ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
			value = "Number of records per page.", defaultValue = "20"),
	})
	public List<CaseSummary>  getActiveCases(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @ApiIgnore Pageable pageMe) {
		return _listService.getActiveCases(caseManagementSystemTag, caseTypeTag, pageMe);
	}

	@RequestMapping(value="summary", method=RequestMethod.GET)
	public Map<String, Number> getSummary(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag) {
		return _listService.getSummaryInfo(caseManagementSystemTag, caseTypeTag);
	}

	@PutMapping(value="/{issueTag}",consumes= {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateIssueListJson(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @PathVariable String issueTag,
			@RequestBody List<Map<String,Object>> csvStream) throws IOException {
		Iterator<Map<String,Object>> valueIterator = csvStream.listIterator();
		List<CaseRequest> newIssueCases = new ArrayList<>();
		valueIterator.forEachRemaining(m -> {
			newIssueCases.add(new MapBasedCaseRequest(m));
		});
		_listService.putIssueList(caseManagementSystemTag, caseTypeTag, issueTag, newIssueCases, ZonedDateTime.now());
		return ResponseEntity.accepted().build();
	}

	private static class MapBasedCaseRequest implements CaseRequest {

		private String _receipt;
		private ZonedDateTime _caseCreation;
		private Map<String, Object> _rest;

		public MapBasedCaseRequest(Map<String, Object> input) {
			_receipt = input.remove("receiptNumber").toString();
			_caseCreation = ZonedDateTime.parse(input.remove("creationDate").toString(), DateTimeFormatter.ISO_DATE_TIME);
			_rest = input;
		}

		@Override
		public String getReceiptNumber() {
			return _receipt;
		}

		@Override
		public ZonedDateTime getCaseCreation() {
			return _caseCreation;
		}

		@Override
		public Map<String, Object> getExtraData() {
			return _rest;
		}
	}
}
