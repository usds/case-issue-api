package gov.usds.case_issues.controllers.crud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;

@RestController
@RequestMapping("/resource/")
public class CaseManagementSystemController {

	Logger LOG = LoggerFactory.getLogger(CaseManagementSystemController.class);
	@Autowired
	private CaseManagementSystemRepository cmsRepo;

	@RequestMapping(method=RequestMethod.GET)
	public Iterable<? extends CaseManagementSystem> getAll() {
		return cmsRepo.findAll();
	}

	@RequestMapping(method=RequestMethod.POST)
	public ResponseEntity<Void> createSystem(@RequestBody CaseManagementSystem submitted) throws URISyntaxException {
		CaseManagementSystem saved = cmsRepo.save(submitted);
		return ResponseEntity.created(new URI("./" + saved.getCaseManagementSystemTag())).build();
	}

	@RequestMapping(path="{tag}", method=RequestMethod.GET)
	public ResponseEntity<CaseManagementSystem> find(@PathVariable("tag") String tag) {
		LOG.info("Trying to fetch '{}'", tag);
		Optional<CaseManagementSystem> found = cmsRepo.findByCaseManagementSystemTag(tag);
		LOG.info("Found {}", found);
		return ResponseEntity.of(found);
	}
}
