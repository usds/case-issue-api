package gov.usds.case_issues.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.services.CaseListService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).READ_CASES.name())")
@RequestMapping("/api/cases/{caseManagementSystemTag}/{caseTypeTag}")
public class HitlistApiController {

	@Autowired
	private CaseListService _listService;

	@GetMapping("search")
	public List<TroubleCase> getCases(
		@PathVariable String caseManagementSystemTag,
		@PathVariable String caseTypeTag,
		@RequestParam("query") String query) {
		return _listService.getCases(caseManagementSystemTag, caseTypeTag, query);
	}

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

	@PutMapping(value="/{issueTag}",consumes= {"text/csv"})
	@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).UPDATE_ISSUES.name())")
	public ResponseEntity<?> updateIssueListCsv(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @PathVariable String issueTag,
			@RequestBody InputStream csvStream) throws IOException {
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		MappingIterator<Map<String, Object>> valueIterator = new CsvMapper()
			.readerFor(Map.class)
			.with(schema)
			.readValues(csvStream);
		List<CaseRequest> newIssueCases = new ArrayList<>();
		valueIterator.forEachRemaining(m -> newIssueCases.add(new MapBasedCaseRequest(m)));
		_listService.putIssueList(caseManagementSystemTag, caseTypeTag, issueTag, newIssueCases, ZonedDateTime.now());
		return ResponseEntity.accepted().build();
	}

	@PreAuthorize("hasAuthority(T(gov.usds.case_issues.authorization.CaseIssuePermission).UPDATE_ISSUES.name())")
	@PutMapping(value="/{issueTag}",consumes= {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateIssueListJson(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @PathVariable String issueTag,
			@RequestBody List<Map<String,Object>> jsonData) throws IOException {
		Iterator<Map<String,Object>> valueIterator = jsonData.listIterator();
		List<CaseRequest> newIssueCases = new ArrayList<>();
		valueIterator.forEachRemaining(m -> newIssueCases.add(new MapBasedCaseRequest(m)));
		_listService.putIssueList(caseManagementSystemTag, caseTypeTag, issueTag, newIssueCases, ZonedDateTime.now());
		return ResponseEntity.accepted().build();
	}

	private static class MapBasedCaseRequest implements CaseRequest {

		private String _receipt;
		private ZonedDateTime _caseCreation;
		private Map<String, Object> _rest;

		public MapBasedCaseRequest(Map<String, Object> input) {
			_receipt = input.remove(SampleDataFileSpec.DEFAULT_RECEIPT_NUMBER_KEY).toString();
			_caseCreation = ZonedDateTime.parse(
				input.remove(SampleDataFileSpec.DEFAULT_CREATION_DATE_KEY).toString(),
				SampleDataFileSpec.DEFAULT_DATETIME_FORMAT
			);
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
