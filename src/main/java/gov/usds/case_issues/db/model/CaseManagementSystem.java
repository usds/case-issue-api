package gov.usds.case_issues.db.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A case management system. In production, instances will probably correspond to live
 * case managers for different teams; this can also be used to distinguish between
 * different testing instances of a single case management system. 
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class CaseManagementSystem extends UpdatableEntity {

	@NaturalId
	@JsonProperty("tag")
	@NotNull
	@Pattern(regexp="[-\\w]+")
	private String caseManagementSystemTag;
	@NotNull
	private String name;
	private String description;
	private String applicationUrl;
	private String caseDetailsUrlTemplate;

	protected CaseManagementSystem() { /* for hibernate */ }

	public CaseManagementSystem(@NotNull @Pattern(regexp = "[-\\w]+") String tag, @NotNull String name, String description) {
		this();
		this.caseManagementSystemTag = tag;
		this.name = name;
		this.description = description;
	}

	public CaseManagementSystem(@NotNull @Pattern(regexp = "[-\\w]+") String tag,
			@NotNull String name, String description, String applicationUrl, String caseDetailsUrlTemplate) {
		this(tag, name, description);
		this.applicationUrl = applicationUrl;
		this.caseDetailsUrlTemplate = caseDetailsUrlTemplate;
	}

	public String getCaseManagementSystemTag() {
		return caseManagementSystemTag;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getApplicationUrl() {
		return applicationUrl;
	}

	public String getCaseDetailsUrlTemplate() {
		return caseDetailsUrlTemplate;
	}
}
