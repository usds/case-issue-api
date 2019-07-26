package gov.usds.case_issues.model;

public interface ApiViews {

	/**
	 * Base JSON view, for things that must always be included.
	 */
	public interface All {}
	/**
	 * A JSON view to be used for multi-entity views. Or possibly for all API views, if we don't
	 * eliminate the possible circular reference in the output.
	 */
	public interface Summary extends All {}

}
