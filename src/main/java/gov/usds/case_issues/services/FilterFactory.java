package gov.usds.case_issues.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gov.usds.case_issues.db.JsonOperatorContributor;
import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.model.AttachmentRequest;
import gov.usds.case_issues.model.DateRange;
import gov.usds.case_issues.services.model.CaseFilter;

@Service
public class FilterFactory {

	private static final Logger LOG = LoggerFactory.getLogger(FilterFactory.class);

	private static final class MetaModel {

		private static final String CASE_CREATION = "caseCreation";
		private static final String CASE_DETAIL_FIELDS = "extraData";
		private static final String SNOOZE_REASON = "snoozeReason";

		private static final String ID = "internalId";
		private static final String EXTERNAL_ID = "externalId";
	}

	public static CaseFilter dateRange(DateRange dateRange) {
		CaseFilter dateSpec = (root, query, cb) -> cb.and(
			cb.greaterThanOrEqualTo(root.get(MetaModel.CASE_CREATION), dateRange.getStartDate()),
			cb.lessThanOrEqualTo(root.get(MetaModel.CASE_CREATION), dateRange.getEndDate())
		);
		return dateSpec;
	}

	public static CaseFilter caseExtraData(Map<String, Object> fieldFilter) {
		String jsonQuery = new JSONObject(fieldFilter).toString();
		return (root, query, cb) -> cb.isTrue(
			cb.function(JsonOperatorContributor.JSON_CONTAINS,
				Boolean.class,
				root.get(MetaModel.CASE_DETAIL_FIELDS),
				cb.literal(jsonQuery)
			)
		);
	}

	public static CaseFilter snoozeReason(String reason) {
		return (root, query, cb) -> cb.equal(root.get(MetaModel.SNOOZE_REASON), reason);
	}

	public static CaseFilter hasAttachment(AttachmentRequest attachmentRequest) {
		return (root, query, cb) -> {
			Subquery<CaseAttachmentAssociation> sq = query.subquery(CaseAttachmentAssociation.class);
			List<Predicate> conjunction = new ArrayList<>();
			Root<CaseAttachmentAssociation> aRoot = sq.from(CaseAttachmentAssociation.class);
			sq.select(aRoot);
			Path<CaseAttachment> aPath = aRoot.get("attachment");
			conjunction.add(cb.equal(aPath.get("attachmentType"), cb.literal(attachmentRequest.getNoteType())));
			LOG.debug("Filtering on attachment type {}", attachmentRequest.getNoteType());
			if (attachmentRequest.getSubtype() != null) {
				LOG.debug("Filtering on attachment subtype {}", attachmentRequest.getSubtype());
				conjunction.add(cb.equal(aPath.get("attachmentSubtype").get(MetaModel.EXTERNAL_ID), cb.literal(attachmentRequest.getSubtype())));
			}
			if (attachmentRequest.getContent() != null) {
				LOG.debug("Filtering on content {}", attachmentRequest.getContent());
				conjunction.add(cb.equal(aPath.get("content"), cb.literal(attachmentRequest.getContent())));

			}
			conjunction.add(cb.equal(aRoot.get("snooze").get("snoozeCase").get(MetaModel.ID), root.get(MetaModel.ID)));
			sq.where(conjunction.toArray(new Predicate[0]));
			return cb.exists(sq);
		};
	}
}
