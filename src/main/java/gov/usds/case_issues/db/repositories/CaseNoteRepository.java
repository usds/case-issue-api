package gov.usds.case_issues.db.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.CaseNote;
import gov.usds.case_issues.db.model.NoteSubtype;
import gov.usds.case_issues.db.model.NoteType;

public interface CaseNoteRepository extends CrudRepository<CaseNote, Long> {

	Optional<CaseNote> findByNoteTypeAndNoteSubtypeAndContent(NoteType type, NoteSubtype subtype, String content);
}
