package gov.usds.case_issues.config;

/**
 * Container for configuration of a single CSV or JSON file to be loaded at startup, including the file path
 * and required information about the case type and case management system, as well as the row information
 * defined in {@link DataFormatSpec}.
 */
public class SampleDataFileSpec extends DataFormatSpec {

	private String caseManagementSystem;
	private String caseType;
	private String filename;

	public String getCaseManagementSystem() {
		return caseManagementSystem;
	}

	public String getCaseType() {
		return caseType;
	}

	public String getFilename() {
		return filename;
	}

	public void setCaseManagementSystem(String caseManagementSystem) {
		this.caseManagementSystem = caseManagementSystem;
	}

	public void setCaseType(String caseType) {
		this.caseType = caseType;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
}
