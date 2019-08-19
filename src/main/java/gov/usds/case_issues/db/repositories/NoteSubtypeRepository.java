package gov.usds.case_issues.db.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.NoteSubtype;

public interface NoteSubtypeRepository extends CrudRepository<NoteSubtype, Long> {

	Optional<NoteSubtype> findByNoteSubtypeTag(String tag);
}
