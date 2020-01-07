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
import gov.usds.case_issues.db.model.AttachmentType;
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
		AttachmentType requestedType = request.getNoteType();
		String requestedSubtype = request.getSubtype();

		AttachmentSubtype subType = null;
		LOG.debug("Attempting to attach note {} {} {}", requestedType, requestedSubtype, request.getContent());
		if (requestedType.requiresSubtype()) {
			if (null == requestedSubtype) {
				throw new IllegalArgumentException("Subtype is required for attachment type " + requestedType);
			}
			subType = _subtypeRepository
				.findByExternalId(requestedSubtype)
				.orElseThrow(() -> new IllegalArgumentException("Invalid subtype"));
			if (subType.getForAttachmentType() != requestedType) {
				throw new IllegalArgumentException("Requested subtype belongs to attachment type " + subType.getForAttachmentType()
					+ ", not " + requestedType);
			}
		} else if (null != requestedSubtype) {
			throw new IllegalArgumentException("Subtypes are not allowed for attachment type " + requestedType);
		}

		CaseAttachment attachment = null;
		Optional<CaseAttachment> search = _attachmentRepository.findByAttachmentTypeAndAttachmentSubtypeAndContent(requestedType, subType, request.getContent());
		if (search.isPresent()) {
			attachment = search.get();
			LOG.debug("Found existing note {}", attachment.getInternalId());
		} else {
			attachment = _attachmentRepository.save(new CaseAttachment(requestedType, subType, request.getContent()));
		}

		return _associationRepository.save(new CaseAttachmentAssociation(snooze, attachment));
	}

	public List<CaseAttachmentAssociation> findAttachmentsForCase(TroubleCase rootCase) {
		return _associationRepository.findAllBySnoozeSnoozeCaseOrderByUpdatedAtAsc(rootCase);
	}
}
