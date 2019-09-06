package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * A case management system. In production, instances will probably correspond to live
 * case managers for different teams; this can also be used to distinguish between
 * different testing instances of a single case management system. 
 */
@Entity
public class CaseManagementSystem extends TaggedEntity {

	private String applicationUrl;
	private String caseDetailsUrlTemplate;

	protected CaseManagementSystem() { /* for hibernate */ }

	public CaseManagementSystem(@NotNull @Pattern(regexp = "[-\\w]+") String tag, @NotNull String name, String description) {
		super(tag, name, description);
	}

	public CaseManagementSystem(@NotNull @Pattern(regexp = "[-\\w]+") String tag,
			@NotNull String name, String description, String applicationUrl, String caseDetailsUrlTemplate) {
		this(tag, name, description);
		this.applicationUrl = applicationUrl;
		this.caseDetailsUrlTemplate = caseDetailsUrlTemplate;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

	public String getCaseDetailsUrlTemplate() {
		return caseDetailsUrlTemplate;
	}
}
