package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonProperty;

@MappedSuperclass
public abstract class TaggedEntity extends UpdatableEntity {

	@NaturalId
	@NotNull
	@Column(nullable=false)
	@JsonProperty("tag")
	@Pattern(regexp="[-\\w]+")
	private String externalId;
	@NotNull
	@Column(nullable=false)
	private String name;
	private String description;

	protected TaggedEntity() { /* for hibernate */ }

	protected TaggedEntity(String externalId, @NotNull String name, String description) {
		super();
		this.externalId = externalId;
		this.name = name;
		this.description = description;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
