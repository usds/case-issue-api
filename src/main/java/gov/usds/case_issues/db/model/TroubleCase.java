package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

/**
 * A case that has been seen, at some point, by this system
 * (and hence must, at some point, have had an issue).
 */
/* And yes, "Case" would be a simpler name, until you remember that it's a reserved word in every language ever */
@Entity
@DynamicUpdate
@TypeDef(name="json", typeClass=JsonStringType.class)
@NamedNativeQueries({
	@NamedNativeQuery(
		name = "snoozed",
		query = "SELECT * from ( "+ TroubleCase.CASE_DTO_QUERY + ") "
			  + "WHERE last_snooze_end >= CURRENT_TIMESTAMP "
			  + "ORDER BY last_snooze_end ASC, case_creation ASC, internal_id ASC",
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "unSnoozed",
		query = "SELECT * from ( "+ TroubleCase.CASE_DTO_QUERY + ") "
			  + "WHERE last_snooze_end is null or last_snooze_end < CURRENT_TIMESTAMP "
			  + "ORDER BY case_creation ASC, internal_id ASC",
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozed.count",
		query = "SELECT count(1) as entity_count from ( "+ TroubleCase.CASE_DTO_QUERY + ") "
			  + "WHERE last_snooze_end >= CURRENT_TIMESTAMP ",
		resultSetMapping = "rowCount"
	),
	@NamedNativeQuery(
		name = "unSnoozed.count",
		query = "SELECT count(1) as entity_count from ( "+ TroubleCase.CASE_DTO_QUERY + ") "
			  + "WHERE last_snooze_end is null or last_snooze_end < CURRENT_TIMESTAMP ",
		resultSetMapping = "rowCount"
	),
	@NamedNativeQuery(
		name = "summary",
		query = "SELECT " + TroubleCase.CASE_SNOOZE_DECODE + " as snooze_state, count(1) "
				+ "FROM ( " + TroubleCase.CASE_DTO_QUERY + ") "
				+ "GROUP BY " + TroubleCase.CASE_SNOOZE_DECODE
	),
})
@SqlResultSetMappings({
	@SqlResultSetMapping(
		name="snoozeCaseMapping",
		entities=@EntityResult(entityClass=TroubleCase.class),
		columns=@ColumnResult(name="last_snooze_end", type=ZonedDateTime.class)
	),
	@SqlResultSetMapping(
		name="rowCount",
		columns=@ColumnResult(name="entity_count", type=Long.class)
	),
})
public class TroubleCase extends UpdatableEntity {

	public static final String CASE_SNOOZE_DECODE =
		"case when last_snooze_end is null then 'NEVER_SNOOZED' "
		+ "when last_snooze_end < CURRENT_TIMESTAMP then 'PREVIOUSLY_SNOOZED' "
		+ "else 'CURRENTLY_SNOOZED' end";
	public static final String CASE_DTO_QUERY =
		"SELECT c.*, "
		+ "(SELECT MAX(snooze_end) FROM case_snooze s where s.snooze_case_internal_id = c.internal_id) last_snooze_end "
		+ "FROM trouble_case c "
		+ "WHERE case_management_system_internal_id = :caseManagementSystemId "
		+ "AND case_type_internal_id = :caseTypeId "
		+ "AND exists ("
			+ "select openissues1_.internal_id "
			+ "from case_issue openissues1_ "
			+ "where c.internal_id=openissues1_.issue_case_internal_id "
			+ "and ( openissues1_.issue_closed is null)"
		+ ")";

	@NaturalId
	@ManyToOne(optional=false)
	@JoinColumn(updatable=false)
	private CaseManagementSystem caseManagementSystem;
	@NaturalId
	@NotNull
	@Pattern(regexp="[-\\w]+")
	@Column(updatable=false)
	private String receiptNumber;

	@ManyToOne(optional=false)
	@JoinColumn(nullable=false, updatable=false)
	private CaseType caseType;
	@NotNull
	private ZonedDateTime caseCreation;

	@OneToMany(mappedBy = "issueCase")
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


	public CaseManagementSystem getCaseManagementSystem() {
		return caseManagementSystem;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public CaseType getCaseType() {
		return caseType;
	}

	public ZonedDateTime getCaseCreation() {
		return caseCreation;
	}

	@JsonIgnore
	public List<CaseIssue> getOpenIssues() {
		return openIssues;
	}

	public Map<String, Object> getExtraData() {
		return extraData;
	}
}
