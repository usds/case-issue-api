package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import gov.usds.case_issues.config.SampleDataConfig.ColumnSpec;
import gov.usds.case_issues.config.SampleDataConfig.SampleDataFileSpec;
import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;

@Service
@Profile({"dev"})
public class CsvLoader {

	private static final Logger LOG = LoggerFactory.getLogger(CsvLoader.class);

	private TroubleCaseRepository caseRepo;
	private CaseManagementSystemRepository caseManagerRepo;
	private CaseTypeRepository caseTypeRepo;
	private CaseIssueRepository caseIssueRepo;
	private CaseSnoozeRepository snoozeRepo;

	public CsvLoader(TroubleCaseRepository caseRepo, CaseManagementSystemRepository caseManagerRepo,
			CaseTypeRepository caseTypeRepo, CaseIssueRepository caseIssueRepo, CaseSnoozeRepository snoozeRepo) {
		super();
		this.caseRepo = caseRepo;
		this.caseManagerRepo = caseManagerRepo;
		this.caseTypeRepo = caseTypeRepo;
		this.caseIssueRepo = caseIssueRepo;
		this.snoozeRepo = snoozeRepo;
	}

	@Transactional
	public <T extends Iterator<Map<String,String>>> void loadAll(T values, SampleDataFileSpec fileConfig) {
		CaseManagementSystem defaultSystem = caseManagerRepo.findByCaseManagementSystemTag(fileConfig.getCaseManagementSystem())
				.orElseThrow(() -> new RuntimeException("Couldn't find specified case management system."));
		CaseType defaultType = caseTypeRepo.findByCaseTypeTag(fileConfig.getCaseType())
				.orElseThrow(() -> new RuntimeException("Couldn't find specified case type."));
		ZonedDateTime now = ZonedDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fileConfig.getCreationDateFormat());
		AtomicInteger i = new AtomicInteger(0);
		Consumer<Map<String,String>> r = row -> {
			String receipt = row.get(fileConfig.getReceiptNumberKey());
			ZonedDateTime creationDate = ZonedDateTime.parse(row.get(fileConfig.getCreationDateKey()), formatter);

			Map<String, Object> extras = new HashMap<>();
			for (ColumnSpec spec : fileConfig.getExtraDataKeys()) {
				Object storedValue = spec.getStoredValue(row);
				extras.put(spec.getInternalKey(), storedValue);
			}

			TroubleCase caseData = caseRepo.save(new TroubleCase(defaultSystem, receipt, defaultType, creationDate, extras));
			caseIssueRepo.save(new CaseIssue(caseData, "OLD", now));
			if (0 == i.incrementAndGet() % 10) {
				snoozeRepo.save(new CaseSnooze(caseData, "Just Because", 2));
			}
		};
		values.forEachRemaining(r);
		LOG.info("Creating {} cases/issues", i.get());
	}
}
