package gov.usds.case_issues.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import gov.usds.case_issues.db.model.NoteType;

@ConfigurationProperties("sample-data")
@EnableConfigurationProperties
@Configuration
@Profile({"dev","test"})
public class SampleDataConfig {

	private List<SampleDataFileSpec> files = new ArrayList<>();
	private List<TaggedResource> caseManagementSystems = new ArrayList<>();;
	private List<TaggedResource> caseTypes = new ArrayList<>();
	private List<NoteSubtypeDefinition> noteSubtypes = new ArrayList<>();

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

	public List<NoteSubtypeDefinition> getNoteSubtypes() {
		return noteSubtypes;
	}

	public void setNoteSubtypes(List<NoteSubtypeDefinition> noteSubtypes) {
		this.noteSubtypes = noteSubtypes;
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

		public ColumnSpec() {
			super();
		}

		public ColumnSpec(String internalKey, String externalKey, ColumnType columnType) {
			this();
			this.internalKey = internalKey;
			this.externalKey = externalKey;
			this.columnType = columnType;
		}

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

	public static class NoteSubtypeDefinition extends TaggedResource {
		private String urlTemplate;
		private NoteType noteType;

		public String getUrlTemplate() {
			return urlTemplate;
		}
		public NoteType getNoteType() {
			return noteType;
		}
		public void setUrlTemplate(String urlTemplate) {
			this.urlTemplate = urlTemplate;
		}
		public void setNoteType(NoteType noteType) {
			this.noteType = noteType;
		}
	}
}
