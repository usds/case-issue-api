package gov.usds.case_issues.db.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonProperty;

@MappedSuperclass
public abstract class TaggedEntity extends UpdatableEntity {

	@NaturalId
	@Column(nullable=false)
	@JsonProperty("tag")
	@Pattern(regexp="[-\\w]+")
	private String externalId;

	protected TaggedEntity() { /* for hibernate */ }

	protected TaggedEntity(String externalId) {
		super();
		this.externalId = externalId;
	}

	public String getExternalId() {
		return externalId;
	}
}
