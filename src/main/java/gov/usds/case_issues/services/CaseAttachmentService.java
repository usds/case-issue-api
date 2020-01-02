package gov.usds.case_issues.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseAttachment;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseAttachmentAssociation;
import gov.usds.case_issues.db.model.AttachmentSubtype;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseAttachmentRepository;
import gov.usds.case_issues.db.repositories.AttachmentAssociationRepository;
import gov.usds.case_issues.db.repositories.AttachmentSubtypeRepository;
import gov.usds.case_issues.model.AttachmentRequest;

@Service
@Transactional(readOnly=true)
public class CaseAttachmentService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseAttachmentService.class);

	@Autowired
	private AttachmentSubtypeRepository _subtypeRepository;
	@Autowired
	private AttachmentAssociationRepository _associationRepository;
	@Autowired
	private CaseAttachmentRepository _attachmentRepository;

	@Transactional(readOnly=false)
	public CaseAttachmentAssociation attachToSnooze(AttachmentRequest request, CaseSnooze snooze) {
		AttachmentSubtype subType = null;
		CaseAttachment attachment = null;

		LOG.debug("Attempting to attach note {} {} {}", request.getNoteType(), request.getSubtype(), request.getContent());
		if (null != request.getSubtype()) {
			subType = _subtypeRepository
				.findByExternalId(request.getSubtype())
				.orElseThrow(() -> new IllegalArgumentException("Invalid subtype"));
		}
		Optional<CaseAttachment> search = _attachmentRepository.findByAttachmentTypeAndAttachmentSubtypeAndContent(request.getNoteType(), subType, request.getContent());
		if (search.isPresent()) {
			attachment = search.get();
			LOG.debug("Found existing note {}", attachment.getInternalId());
		} else {
			attachment = _attachmentRepository.save(new CaseAttachment(request.getNoteType(), subType, request.getContent()));
		}

		return _associationRepository.save(new CaseAttachmentAssociation(snooze, attachment));
	}

	public List<CaseAttachmentAssociation> findAttachmentsForCase(TroubleCase rootCase) {
		return _associationRepository.findAllBySnoozeSnoozeCaseOrderByUpdatedAtAsc(rootCase);
	}
}
