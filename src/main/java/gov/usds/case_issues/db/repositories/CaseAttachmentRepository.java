package gov.usds.case_issues.db.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.AttachmentSubtype;
import gov.usds.case_issues.db.model.AttachmentType;

public interface CaseAttachmentRepository extends CrudRepository<CaseAttachment, Long> {

	Optional<CaseAttachment> findByAttachmentTypeAndAttachmentSubtypeAndContent(
			AttachmentType type, AttachmentSubtype subtype, String content);
}
