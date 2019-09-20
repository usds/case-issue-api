package gov.usds.case_issues.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="sample-data", ignoreUnknownFields=false)
@Component
@Lazy
@Profile({"dev"})
public class SampleDataConfig {

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
}
