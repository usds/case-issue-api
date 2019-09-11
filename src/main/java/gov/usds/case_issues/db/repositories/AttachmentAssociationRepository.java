package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.TroubleCase;

public interface AttachmentAssociationRepository extends CrudRepository<CaseAttachmentAssociation, Long> {

	public List<CaseAttachmentAssociation> findAllBySnoozeSnoozeCaseOrderByUpdatedAtAsc(TroubleCase rootCase);
}
