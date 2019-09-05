package gov.usds.case_issues.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.usds.case_issues.db.model.CaseNote;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.NoteAssociation;
import gov.usds.case_issues.db.model.NoteSubtype;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseNoteRepository;
import gov.usds.case_issues.db.repositories.NoteAssociationRepository;
import gov.usds.case_issues.db.repositories.NoteSubtypeRepository;
import gov.usds.case_issues.model.NoteRequest;

@Service
@Transactional(readOnly=true)
public class CaseAttachmentService {

	private static final Logger LOG = LoggerFactory.getLogger(CaseAttachmentService.class);

	@Autowired
	private NoteSubtypeRepository _subtypeRepository;
	@Autowired
	private NoteAssociationRepository _associationRepository;
	@Autowired
	private CaseNoteRepository _noteRepository;
 
	@Transactional(readOnly=false)
	public NoteAssociation attachNote(NoteRequest request, CaseSnooze snooze) {
		NoteSubtype subType = null;
		CaseNote note = null;

		LOG.debug("Attempting to attach note {} {} {}", request.getNoteType(), request.getSubtype(), request.getContent());
		if (null != request.getSubtype()) {
			subType = _subtypeRepository.findByExternalId(request.getSubtype())
				.orElseThrow(IllegalArgumentException::new);
		}
		Optional<CaseNote> noteSearch = _noteRepository.findByNoteTypeAndNoteSubtypeAndContent(request.getNoteType(), subType, request.getContent());
		if (noteSearch.isPresent()) {
			note = noteSearch.get();
			LOG.debug("Found existing note {}", note.getInternalId());
		} else {
			note = _noteRepository.save(new CaseNote(request.getNoteType(), subType, request.getContent()));
		}

		return _associationRepository.save(new NoteAssociation(snooze, note));
	}

	public List<NoteAssociation> findNotesForCase(TroubleCase rootCase) {
		return _associationRepository.findAllBySnoozeSnoozeCaseOrderByUpdatedAtAsc(rootCase);
	}
}
