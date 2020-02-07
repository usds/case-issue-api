package gov.usds.case_issues.model;

import java.util.List;

/**
 * API container for cases and their counts
 */
public class CaseListResponse {

	private List<? extends CaseSummary> cases;
	private long totalCount;
	private long queryCount;

	public CaseListResponse(List<? extends CaseSummary> cases, long totalCount, long queryCount) {
		super();
		this.cases = cases;
		this.totalCount = totalCount;
		this.queryCount = queryCount;
	}

	public List<? extends CaseSummary> getCases() {
		return cases;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public long getQueryCount() {
		return queryCount;
	}
}
