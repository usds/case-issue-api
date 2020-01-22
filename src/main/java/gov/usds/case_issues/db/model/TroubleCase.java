package gov.usds.case_issues.db.model;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.DynamicUpdate;
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
	/* ALL SNOOZED CASES */
	@NamedNativeQuery(
		name = "snoozedFirstPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozedFirstPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozedLaterPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.SNOOZED_PAGE_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozedLaterPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.SNOOZED_PAGE_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	/* SNOOZED WITH SPECIFIC REASON */
	@NamedNativeQuery(
		name = "snoozeReasonFirstPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_SPECIFIC_REASON_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozeReasonFirstPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_SPECIFIC_REASON_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozeReasonLaterPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_SPECIFIC_REASON_CONSTRAINT
				+ TroubleCase.SNOOZED_PAGE_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "snoozeReasonLaterPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_NOW_SPECIFIC_REASON_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.SNOOZED_PAGE_CONSTRAINT
				+ TroubleCase.SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	/* ALL NON-SNOOZED CASES */
	@NamedNativeQuery(
		name = "notCurrentlySnoozedFirstPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.NOT_SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "notCurrentlySnoozedFirstPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.NOT_SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "notCurrentlySnoozedLaterPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.NOT_SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_PAGE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "notCurrentlySnoozedLaterPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.NOT_SNOOZED_NOW_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_PAGE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	/* PREVIOUSLY BUT NOT CURRENTLY SNOOZED CASES */
	@NamedNativeQuery(
		name = "previouslySnoozedFirstPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_PREVIOUSLY_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "previouslySnoozedFirstPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_PREVIOUSLY_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "previouslySnoozedLaterPage",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_PREVIOUSLY_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_PAGE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "previouslySnoozedLaterPageDateFilter",
		query = TroubleCase.CASE_SELECT_STEM
				+ TroubleCase.SNOOZED_PREVIOUSLY_CONSTRAINT
				+ TroubleCase.CASE_CREATION_DATE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_PAGE_CONSTRAINT
				+ TroubleCase.NOT_SNOOZED_NOW_POSTAMBLE,
		resultSetMapping="snoozeCaseMapping"
	),
	@NamedNativeQuery(
		name = "summary",
		query = "SELECT " + TroubleCase.CASE_SNOOZE_DECODE + " as snooze_state, count(1) "
				+ "FROM " + TroubleCase.CASE_DTO_CTE
				+ "GROUP BY " + TroubleCase.CASE_SNOOZE_DECODE
	),
	/* Resolved Cases */
	@NamedNativeQuery(
		name = "resolvedCount",
		query = TroubleCase.RESOLVED_CASE_COUNT
	),
	@NamedNativeQuery(
		name = "averageDaysToResoluton",
		query = TroubleCase.AVERAGE_DAYS_TO_RESOLUTION
	),
	@NamedNativeQuery(
		name = "averageDaysWorked",
		query = TroubleCase.AVERAGE_DAYS_WORKED
	),
})
@SqlResultSetMappings({
	@SqlResultSetMapping(
		name="snoozeCaseMapping",
		entities=@EntityResult(entityClass=TroubleCase.class),
		columns=@ColumnResult(name="last_snooze_end", type=ZonedDateTime.class)
	),
})

public class TroubleCase extends TroubleCaseFixedData {
	public static final String RESOLVED_CASE_COUNT =
		"SELECT COUNT(DISTINCT c.internal_id) "
		+ "FROM {h-schema}trouble_case c "
		+ "WHERE case_management_system_internal_id = :caseManagementSystemId "
		+ "AND case_type_internal_id = :caseTypeId "
		// case has been closed
		+ "AND EXISTS ("
			+ "SELECT openissues1_.internal_id "
			+ "FROM {h-schema}case_issue openissues1_ "
			+ "WHERE c.internal_id=openissues1_.issue_case_internal_id "
			+ "AND ( openissues1_.issue_closed IS NOT null) "
			+ "AND openissues1_.issue_closed BETWEEN :caseClosedWindowStart and :caseClosedWindowEnd "
		+ ") "
		// case has been snoozed
		+ "AND EXISTS ("
			+ "	SELECT *"
			+ " FROM {h-schema}case_snooze s "
			+ " WHERE s.snooze_case_internal_id = c.internal_id "
		+ ")";

	public static final String AVERAGE_DAYS_TO_RESOLUTION =
		"SELECT COALESCE(AVG(DATE_PART('day', i.issue_closed - c.case_creation)), 0) "
		+ "FROM {h-schema}trouble_case c "
		+ "INNER JOIN {h-schema}case_issue i "
		+ "ON c.internal_id = i.issue_case_internal_id AND ( i.issue_closed IS NOT NULL) "
		+ "WHERE case_management_system_internal_id = :caseManagementSystemId "
		+ "AND case_type_internal_id = :caseTypeId "
		// case has been closed
		+ "AND EXISTS ("
			+ "SELECT DISTINCT ON(openissues1_.internal_id) * "
			+ "FROM {h-schema}case_issue openissues1_ "
			+ "WHERE c.internal_id=openissues1_.issue_case_internal_id "
			+ "AND ( openissues1_.issue_closed IS NOT NULL) "
			+ "AND openissues1_.issue_closed BETWEEN :caseClosedWindowStart AND :caseClosedWindowEnd "
			+ "ORDER BY openissues1_.internal_id, openissues1_.issue_created DESC"
		+ ") "
		// case has been snoozed
		+ "AND EXISTS ("
			+ "	SELECT *"
			+ " FROM {h-schema}case_snooze s "
			+ " WHERE s.snooze_case_internal_id = c.internal_id "
		+ ")";

		public static final String AVERAGE_DAYS_WORKED =
		"SELECT COALESCE(AVG(DATE_PART('day', i.issue_closed - s.created_at)), 0) "
		+ "FROM {h-schema}trouble_case c "
		+ "INNER JOIN {h-schema}case_issue i "
		+ "ON c.internal_id = i.issue_case_internal_id AND ( i.issue_closed IS NOT NULL) "
		+ "INNER JOIN ( "
			+ "SELECT DISTINCT ON(s1.snooze_case_internal_id) * "
			+ "FROM {h-schema}case_snooze s1 "
			+ "ORDER BY s1.snooze_case_internal_id, s1.created_at ASC "
		+ ") s "
		+ "ON c.internal_id = s.snooze_case_internal_id "
		+ "WHERE case_management_system_internal_id = :caseManagementSystemId "
		+ "AND case_type_internal_id = :caseTypeId "
		// case has been closed
		+ "AND EXISTS ("
			+ "SELECT DISTINCT ON(openissues1_.internal_id) * "
			+ "FROM {h-schema}case_issue openissues1_ "
			+ "WHERE c.internal_id=openissues1_.issue_case_internal_id "
			+ "AND ( openissues1_.issue_closed IS NOT NULL) "
			+ "AND openissues1_.issue_closed BETWEEN :caseClosedWindowStart AND :caseClosedWindowEnd "
			+ "ORDER BY openissues1_.internal_id, openissues1_.issue_created DESC"
		+ ")";

	public static final String CASE_SNOOZE_DECODE =
		"case when last_snooze_end is null then 'NEVER_SNOOZED' "
		+ "when last_snooze_end < CURRENT_TIMESTAMP then 'PREVIOUSLY_SNOOZED' "
		+ "else 'CURRENTLY_SNOOZED' end";

	public static final String CASE_DTO_QUERY =
		"SELECT c.*, "
		+ "(SELECT MAX(snooze_end) FROM {h-schema}case_snooze s where s.snooze_case_internal_id = c.internal_id) last_snooze_end "
		+ "FROM {h-schema}trouble_case c "
		+ "WHERE case_management_system_internal_id = :caseManagementSystemId "
		+ "AND case_type_internal_id = :caseTypeId "
		+ "AND exists ("
			+ "select openissues1_.internal_id "
			+ "from {h-schema}case_issue openissues1_ "
			+ "where c.internal_id=openissues1_.issue_case_internal_id "
			+ "and ( openissues1_.issue_closed is null)"
		+ ")";

	public static final String NOT_SNOOZED_NOW_CONSTRAINT = " (last_snooze_end IS NULL OR last_snooze_end < CURRENT_TIMESTAMP) ";
	public static final String SNOOZED_NOW_CONSTRAINT = " last_snooze_end >= CURRENT_TIMESTAMP ";
	public static final String SNOOZED_PREVIOUSLY_CONSTRAINT = " last_snooze_end < CURRENT_TIMESTAMP ";
	public static final String SNOOZED_NOW_SPECIFIC_REASON_CONSTRAINT =
			" exists ("
			+ "	SELECT *"
			+ " FROM {h-schema}case_snooze s "
			+ " WHERE s.snooze_case_internal_id = trouble_case_dto.internal_id "
			+ " AND s.snooze_reason = :snoozeReason "
			+ " AND s.snooze_end >= CURRENT_TIMESTAMP "
			+ ") ";

	public static final String CASE_CREATION_DATE_CONSTRAINT =
		" AND case_creation BETWEEN :caseCreationWindowStart and :caseCreationWindowEnd "; // BETWEEN treats the endpoint values as included in the range.


	private static final String CREATION_TO_RECEIPT_FALLBACK =
		" case_creation = :caseCreation AND receipt_number > "
		+ " (select receipt_number from {h-schema}trouble_case where internal_id = :internalId)";
	public static final String NOT_SNOOZED_NOW_PAGE_CONSTRAINT =
		" AND (case_creation > :caseCreation OR "
		+ CREATION_TO_RECEIPT_FALLBACK
		+") ";
	public static final String SNOOZED_PAGE_CONSTRAINT =
		"  AND (last_snooze_end > :lastSnoozeEnd"
		+ " OR (last_snooze_end = :lastSnoozeEnd AND case_creation > :caseCreation) "
		+ " OR (last_snooze_end = :lastSnoozeEnd AND " + CREATION_TO_RECEIPT_FALLBACK + ")"
		+ ")";
	public static final String NOT_SNOOZED_NOW_POSTAMBLE =
		"  ORDER BY case_creation ASC, receipt_number ASC "
		+ "LIMIT :size";
	public static final String SNOOZED_NOW_POSTAMBLE =
		"  ORDER BY last_snooze_end ASC, case_creation ASC, receipt_number ASC "
		+ "LIMIT :size";

	public static final String CASE_DTO_CTE = "(" + CASE_DTO_QUERY + ") as trouble_case_dto ";
	public static final String CASE_SELECT_STEM = "SELECT * FROM" + CASE_DTO_CTE + " WHERE ";

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
		super(caseManagementSystem, receiptNumber, caseType, caseCreation);
		this.extraData = new HashMap<String, Object>(extraData); // shallow copy for reasonable safety
	}


	@JsonIgnore
	public List<CaseIssue> getOpenIssues() {
		return openIssues;
	}

	public Map<String, Object> getExtraData() {
		return extraData;
	}
}
