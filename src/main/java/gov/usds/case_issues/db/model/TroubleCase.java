package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

import gov.usds.case_issues.model.ApiViews;

/**
 * A case that has been seen, at some point, by this system
 * (and hence must, at some point, have had an issue).
 */
/* And yes, "Case" would be a simpler name, until you remember that it's a reserved word in every language ever */
@Entity
@TypeDef(name="json", typeClass=JsonStringType.class)
public class TroubleCase {

	@Id
	@GeneratedValue
	@JsonIgnore
	private Long internalCaseId;

	@NaturalId
	@ManyToOne(optional=false)
	private CaseManagementSystem caseManagementSystem;
	@NaturalId
	@NotNull
	@Pattern(regexp="[-\\w]+")
	private String receiptNumber;

	@ManyToOne(optional=false)
	private CaseType caseType;
	@NotNull
	private ZonedDateTime caseCreation;

	@OneToMany(mappedBy="issueCase") // that right?
	@Where(clause="issue_closed is null")
	private List<CaseIssue> openIssues;

	@Type(type="json")
	@Column(columnDefinition = "varchar(32000)")
	private Map<String, Object> extraData;

	protected TroubleCase() {/* for hibernate/JPA */}

	public TroubleCase(CaseManagementSystem caseManagementSystem,
			@NotNull @Pattern(regexp = "[-\\w]+") String receiptNumber, CaseType caseType,
			@NotNull ZonedDateTime caseCreation,
			Map<String,Object> extraData) {
		this();
		this.caseManagementSystem = caseManagementSystem;
		this.receiptNumber = receiptNumber;
		this.caseType = caseType;
		this.caseCreation = caseCreation;
		this.extraData = new HashMap<String, Object>(extraData); // shallow copy for reasonable safety
	}

	public Long getInternalCaseId() {
		return internalCaseId;
	}

	public CaseManagementSystem getCaseManagementSystem() {
		return caseManagementSystem;
	}

	@JsonView(ApiViews.Summary.class)
	public String getReceiptNumber() {
		return receiptNumber;
	}

	public CaseType getCaseType() {
		return caseType;
	}

	@JsonView(ApiViews.Summary.class)
	public ZonedDateTime getCaseCreation() {
		return caseCreation;
	}

	@JsonView(ApiViews.Summary.class)
	public List<CaseIssue> getOpenIssues() {
		return openIssues;
	}

	@JsonView(ApiViews.Summary.class)
	public Map<String, Object> getExtraData() {
		return extraData;
	}
}
