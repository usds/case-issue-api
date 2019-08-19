package gov.usds.case_issues.db.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import gov.usds.case_issues.db.model.NoteAssociation;
import gov.usds.case_issues.db.model.TroubleCase;

public interface NoteAssociationRepository extends CrudRepository<NoteAssociation, Long> {

	public List<NoteAssociation> findAllBySnoozeSnoozeCaseOrderByAssociationTimestampAsc(TroubleCase rootCase);
}
