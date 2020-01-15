package gov.usds.case_issues.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
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

import gov.usds.case_issues.authorization.RequireReadCasePermission;
import gov.usds.case_issues.authorization.RequireUploadPermission;
import gov.usds.case_issues.config.DataFormatSpec;
import gov.usds.case_issues.db.model.AttachmentType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.BulkCaseRepository;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.CaseRequest;
import gov.usds.case_issues.model.CaseSnoozeFilter;
import gov.usds.case_issues.model.CaseSummary;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.services.CaseFilteringService;
import gov.usds.case_issues.services.CaseListService;
import gov.usds.case_issues.services.FilterFactory;
import gov.usds.case_issues.services.IssueUploadService;
import gov.usds.case_issues.services.model.CaseFilter;
import gov.usds.case_issues.services.model.CaseGroupInfo;
import gov.usds.case_issues.validators.FilterParameter;
import gov.usds.case_issues.validators.TagFragment;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequireReadCasePermission
@RequestMapping("/api/cases/{caseManagementSystemTag}/{caseTypeTag}")
@Validated
public class HitlistApiController {

	private static final Logger LOG = LoggerFactory.getLogger(HitlistApiController.class);

	@Autowired
	private CaseListService _listService;
	@Autowired
	private CaseFilteringService _filteringService;
	@Autowired
	private IssueUploadService _uploadService;

	protected static final class FilterParams {
		protected static final String STEM = "filter_";
		protected static final String LINK_BY_TYPE = "hasLinkType";
		protected static final String LINK_BY_CONTENT = "hasLink";
		protected static final String TAG_BY_TYPE = "hasTagType";
		protected static final String TAG_BY_CONTENT = "hasTag";
		protected static final String COMMENT_ANY = "hasAnyComment";
		protected static final String COMMENT_CONTENT = "hasComment";
		protected static final String DATA_FIELD = "dataField";
	}
	private static final Pattern FILTER_PATTERN = Pattern.compile(FilterParams.STEM + "(\\w+)(?:\\[(\\w+)\\])?");

	@GetMapping("search")
	public List<TroubleCase> getCases(
		@PathVariable String caseManagementSystemTag,
		@PathVariable String caseTypeTag,
		@RequestParam("query") @TagFragment String query) {
		return _listService.getCases(caseManagementSystemTag, caseTypeTag, query);
	}

	@ApiImplicitParams({
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.LINK_BY_TYPE,
			value="Has any attached link of this subtype (e.g. 'troubleticket')",
			paramType="query"),
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.LINK_BY_CONTENT + "[$SUBTYPE]",
			value="Has a specific link of the type $SUBTYPE",
			paramType="query"),
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.TAG_BY_TYPE,
			value="Has any attached tag of this subtype (e.g. 'troubleticket')",
			paramType="query"),
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.TAG_BY_CONTENT + "[$SUBTYPE]",
			value="Has a specific tag of the type $SUBTYPE",
			paramType="query"),
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.COMMENT_ANY,
			value="Has at least one comment",
			paramType="query",
			dataType="boolean"),
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.COMMENT_CONTENT,
			value="Has a comment with exactly this content",
			paramType="query"),
		@ApiImplicitParam(
			name=FilterParams.STEM + FilterParams.DATA_FIELD + "[$FIELD_NAME]",
			value="Has the given value for user-defined field $FIELD_NAME",
			paramType="query"),
	})
	@GetMapping
	public List<? extends CaseSummary> getCases(
			@PathVariable @ApiParam(value="The case management system to search in", required=true) String caseManagementSystemTag,
			@PathVariable @ApiParam(value="The type of case to select", required=true) String caseTypeTag,
			@RequestParam(required=true) @ApiParam(value="Which group of cases to select", required=true) Set<CaseSnoozeFilter> mainFilter,
			@RequestParam(required=false) @DateTimeFormat(iso=ISO.DATE_TIME) ZonedDateTime caseCreationRangeBegin,
			@RequestParam(required=false) @DateTimeFormat(iso=ISO.DATE_TIME) ZonedDateTime caseCreationRangeEnd,
			@RequestParam @ApiParam("An opaque parameter that specifies the page to fetch") Optional<String> pageReference,
			@RequestParam Optional<String> snoozeReason,
			@RequestParam(defaultValue = "20") @Range(max=BulkCaseRepository.MAX_PAGE_SIZE) @ApiParam("The maximum records to return") int size,
			@ApiIgnore @RequestParam MultiValueMap<@FilterParameter String, String> allParams
			) {
		if (snoozeReason.isPresent() && !mainFilter.contains(CaseSnoozeFilter.SNOOZED)) {
			throw new IllegalArgumentException("Snooze reason cannot be specified for cases that are not snoozed");
		}
		List<CaseFilter> filters = new ArrayList<>();
		if (caseCreationRangeBegin != null) {
			filters.add(FilterFactory.dateRange(new DateRange(caseCreationRangeBegin, caseCreationRangeEnd)));
		}
		snoozeReason.ifPresent(reason -> filters.add(FilterFactory.snoozeReason(reason)));
		LOG.debug("All Parameters dict has size {} and keys {}", allParams.size(), allParams.keySet());
		for (Entry<String, List<String>> e : allParams.entrySet()) {
			String parameterName = e.getKey();
			Matcher nameMatch = FILTER_PATTERN.matcher(parameterName);
			if (nameMatch.matches()) {
				LOG.debug("Filtering on entry {}", e);
				List<String> parameterValue = e.getValue();
				String firstValue = parameterValue.get(0);
				String parameterRoot = nameMatch.group(1);
				String subParameter = nameMatch.group(2);
				switch(parameterRoot) {
					case FilterParams.LINK_BY_TYPE:
						LOG.debug("Looking for any link with subtype {}", firstValue);
						filters.add(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.LINK, null, firstValue)));
						break;
					case FilterParams.LINK_BY_CONTENT:
						assertSubparameter(parameterRoot, subParameter);
						LOG.debug("Looking for link with subtype {} and value {}", subParameter, firstValue);
						filters.add(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.LINK, firstValue, subParameter)));
						break;
					case FilterParams.TAG_BY_TYPE:
						LOG.debug("Looking for any tag with subtype {}", firstValue);
						filters.add(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.TAG, null, firstValue)));
						break;
					case FilterParams.TAG_BY_CONTENT:
						assertSubparameter(parameterRoot, subParameter);
						LOG.debug("Looking for tag with subtype {} and value {}", subParameter, firstValue);
						filters.add(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.TAG, firstValue, subParameter)));
						break;
					case FilterParams.COMMENT_ANY:
						LOG.debug("Looking for any comment");
						boolean yesNo = Boolean.parseBoolean(firstValue);
						filters.add(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.COMMENT, null), yesNo));
						break;
					case FilterParams.COMMENT_CONTENT:
						LOG.debug("Looking for a comment with text [{}]", firstValue);
						filters.add(FilterFactory.hasAttachment(new AttachmentRequest(AttachmentType.COMMENT, firstValue)));
						break;
					case FilterParams.DATA_FIELD:
						LOG.debug("Filtering on a single data field");
						assertSubparameter(parameterRoot, subParameter);
						filters.add(FilterFactory.caseExtraData(Collections.singletonMap(subParameter, firstValue)));
						break;
					default:
						throw new IllegalArgumentException(String.format("Invalid filter parameter %s", parameterName));
				}
			} else {
				LOG.debug("Parameter {} skipped as non-matching", parameterName);
			}
		}
		LOG.debug("Derived filter list: {}", filters);
		return _filteringService.getCases(caseManagementSystemTag, caseTypeTag, mainFilter, size, Optional.empty(), pageReference, filters);
	}

	private static void assertSubparameter(String parameter, String subParameter) {
		if (null == subParameter || subParameter.isEmpty()) {
			throw new IllegalArgumentException("Parameter " + parameter + " requires a sub-parameter");
		}
	}

	@GetMapping("snoozed")
	public List<? extends CaseSummary> getSnoozedCases(
		@PathVariable String caseManagementSystemTag,
		@PathVariable String caseTypeTag,
		@RequestParam(name = "receiptNumber", defaultValue = "") @TagFragment String receiptNumber,
		@RequestParam(name = "size", defaultValue = "20") Integer size
	) {
		return _listService.getSnoozedCases(caseManagementSystemTag, caseTypeTag, receiptNumber, size);
	}

	@GetMapping("active")
	public List<? extends CaseSummary> getActiveCases(
		@PathVariable String caseManagementSystemTag,
		@PathVariable String caseTypeTag,
		@RequestParam(name = "receiptNumber", defaultValue = "") @TagFragment String receiptNumber,
		@RequestParam(name = "size", defaultValue = "20") Integer size
	) {
		return _listService.getActiveCases(caseManagementSystemTag, caseTypeTag, receiptNumber, size);
	}

	@RequestMapping(value="summary", method=RequestMethod.GET)
	public Map<String, Object> getSummary(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag) {
		return _listService.getSummaryInfo(caseManagementSystemTag, caseTypeTag);
	}

	@PutMapping(value="/{issueTag}",consumes= {"text/csv"})
	@RequireUploadPermission
	public ResponseEntity<?> updateIssueListCsv(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @PathVariable String issueTag,
			@RequestBody InputStream csvStream, @RequestParam(required=false) String uploadSchema) throws IOException {
		CaseGroupInfo translated = _listService.translatePath(caseManagementSystemTag, caseTypeTag);
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		MappingIterator<Map<String, Object>> valueIterator = new CsvMapper()
			.readerFor(Map.class)
			.with(schema)
			.readValues(csvStream);
		List<CaseRequest> newIssueCases = processCaseUploads(valueIterator, uploadSchema);
		_uploadService.putIssueList(translated, issueTag, newIssueCases, ZonedDateTime.now());
		return ResponseEntity.accepted().build();
	}

	@RequireUploadPermission
	@PutMapping(value="/{issueTag}",consumes= {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<?> updateIssueListJson(@PathVariable String caseManagementSystemTag, @PathVariable String caseTypeTag, @PathVariable String issueTag,
			@RequestBody List<Map<String,Object>> jsonData, @RequestParam(required=false) String uploadSchema) throws IOException {
		CaseGroupInfo translated = _listService.translatePath(caseManagementSystemTag, caseTypeTag);
		Iterator<Map<String,Object>> valueIterator = jsonData.listIterator();
		List<CaseRequest> newIssueCases = processCaseUploads(valueIterator, uploadSchema);
		_uploadService.putIssueList(translated, issueTag, newIssueCases, ZonedDateTime.now());
		return ResponseEntity.accepted().build();
	}

	private List<CaseRequest> processCaseUploads(Iterator<Map<String, Object>> valueIterator, String schemaName) {
		List<CaseRequest> newIssueCases = new ArrayList<>();
		DataFormatSpec spec = _listService.getUploadFormat(schemaName);
		try {
			valueIterator.forEachRemaining(m -> newIssueCases.add(new MapBasedCaseRequest(spec, m)));
		} catch (DateTimeParseException badDate) {
			throw new IllegalArgumentException("Incorrectly formatted case creation date in input"); // ... somewhere
		}
		return newIssueCases;
	}

	private static class MapBasedCaseRequest implements CaseRequest {

		private String _receipt;
		private ZonedDateTime _caseCreation;
		private Map<String, Object> _rest;

		public MapBasedCaseRequest(DataFormatSpec spec, Map<String, Object> input) {
			_receipt = input.remove(spec.getReceiptNumberKey()).toString();
			_caseCreation = ZonedDateTime.parse(
				input.remove(spec.getCreationDateKey()).toString(),
				spec.getCreationDateParser()
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
