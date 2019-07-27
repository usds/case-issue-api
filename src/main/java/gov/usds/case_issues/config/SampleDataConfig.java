package gov.usds.case_issues.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@ConfigurationProperties("sample-data")
@EnableConfigurationProperties
@Configuration
@Profile({"dev","test"})
public class SampleDataConfig {

	private List<SampleDataFileSpec> files = new ArrayList<>();
	private List<TaggedResource> caseManagementSystems = new ArrayList<>();;
	private List<TaggedResource> caseTypes = new ArrayList<>();;

	public SampleDataConfig() {
		super();
	}

	public List<SampleDataFileSpec> getFiles() {
		return files;
	}

	public void setFiles(List<SampleDataFileSpec> files) {
		this.files = files;
	}

	public List<TaggedResource> getCaseManagementSystems() {
		return caseManagementSystems;
	}

	public void setCaseManagementSystems(List<TaggedResource> caseManagementSystems) {
		this.caseManagementSystems = caseManagementSystems;
	}

	public List<TaggedResource> getCaseTypes() {
		return caseTypes;
	}

	public void setCaseTypes(List<TaggedResource> caseTypes) {
		this.caseTypes = caseTypes;
	}

	/**
	 * Container for configuration of a single CSV or JSON file to be loaded at startup, including the file path
	 * and required information about the columns/fields expected in each row/object.
	 */
	public static class SampleDataFileSpec {
		private String filename;
		private String creationDateFormat;
		private String receiptNumberKey = "receiptNumber";
		private String creationDateKey = "creationDate";
		private String caseManagementSystem;
		private String caseType;
		private List<ColumnSpec> extraDataKeys;

		public String getCreationDateKey() {
			return creationDateKey;
		}

		public String getCreationDateFormat() {
			return creationDateFormat;
		}

		public List<ColumnSpec> getExtraDataKeys() {
			return extraDataKeys;
		}

		public String getFilename() {
			return filename;
		}

		public String getReceiptNumberKey() {
			return receiptNumberKey;
		}

		public String getCaseManagementSystem() {
			return caseManagementSystem;
		}

		public void setCaseManagementSystem(String caseManagementSystem) {
			this.caseManagementSystem = caseManagementSystem;
		}

		public String getCaseType() {
			return caseType;
		}

		public void setCaseType(String caseType) {
			this.caseType = caseType;
		}

		public void setCreationDateKey(String caseCreationDateKey) {
			this.creationDateKey = caseCreationDateKey;
		}

		public void setCreationDateFormat(String creation_date_format) {
			this.creationDateFormat = creation_date_format;
		}

		public void setExtraDataKeys(List<ColumnSpec> extraDataKeys) {
			this.extraDataKeys = extraDataKeys;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}
		public void setReceiptNumberKey(String receiptNumberKey) {
			this.receiptNumberKey = receiptNumberKey;
		}
	}

	/** Data type of a column, to allow conversion from strings to appropriate native values. */
	public enum ColumnType {
		STRING, INTEGER, BOOLEAN;
	}

	/** Specification for a non-standard column being loaded in from a CSV file or JSON node */
	public static class ColumnSpec {
		private String internalKey;
		private String externalKey;
		private ColumnType columnType = ColumnType.STRING;

		public String getInternalKey() {
			return internalKey;
		}
		public void setInternalKey(String internalKey) {
			this.internalKey = internalKey;
		}
		public String getExternalKey() {
			return externalKey != null ? externalKey : internalKey;
		}
		public void setExternalKey(String externalKey) {
			this.externalKey = externalKey;
		}
		public ColumnType getColumnType() {
			return columnType;
		}
		public void setColumnType(ColumnType columnType) {
			this.columnType = columnType;
		}

		public Object getStoredValue(Map<String, String> row) {
			String inputValue = row.get(getExternalKey());
			switch (getColumnType()) {
				case BOOLEAN:
					return Boolean.valueOf(inputValue);
				case INTEGER:
					return Integer.valueOf(inputValue);
				case STRING:
					return inputValue;
				default: throw new RuntimeException("Failed to handle data type " + getColumnType());
			}
		}
	}

	/** Container for a configured resource that needs a tag and a name (e.g Case Management System or Case Type) */
	public static class TaggedResource {
		private String tag;
		private String name;
		private String description;

		public String getTag() {
			return tag;
		}
		public void setTag(String tag) {
			this.tag = tag;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}
}
