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
import gov.usds.case_issues.config.SampleDataFileSpec;
import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseSnooze;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseSnoozeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;
import gov.usds.case_issues.services.CaseListService.CaseGroupInfo;

@Service
@Profile({"dev", "servicetest"})
public class CsvLoader {

	private static final Logger LOG = LoggerFactory.getLogger(CsvLoader.class);
	/** One in ever this-many cases will get snoozed at start-up time, to make the data more interesting */
	private static final int FAKE_SNOOZE_INTERVAL = 10;

	private TroubleCaseRepository caseRepo;
	private CaseIssueRepository caseIssueRepo;
	private CaseSnoozeRepository snoozeRepo;
	private CaseListService listService;

	public CsvLoader(TroubleCaseRepository caseRepo, CaseIssueRepository caseIssueRepo,
			CaseSnoozeRepository snoozeRepo, CaseListService caseListService) {
		super();
		this.caseRepo = caseRepo;
		this.caseIssueRepo = caseIssueRepo;
		this.snoozeRepo = snoozeRepo;
		this.listService = caseListService;
	}

	@Transactional
	public <T extends Iterator<Map<String,String>>> void loadAll(T values, SampleDataFileSpec fileConfig) {
		CaseGroupInfo caseGroup = listService.translatePath(fileConfig.getCaseManagementSystem(), fileConfig.getCaseType());
		ZonedDateTime now = ZonedDateTime.now();
		DateTimeFormatter formatter = fileConfig.getCreationDateParser();
		AtomicInteger i = new AtomicInteger(0);
		Consumer<Map<String,String>> r = row -> {
			String receipt = row.get(fileConfig.getReceiptNumberKey());
			ZonedDateTime creationDate = ZonedDateTime.parse(row.get(fileConfig.getCreationDateKey()), formatter);

			Map<String, Object> extras = new HashMap<>();
			for (ColumnSpec spec : fileConfig.getExtraDataKeys()) {
				Object storedValue = spec.getStoredValue(row);
				extras.put(spec.getInternalKey(), storedValue);
			}

			TroubleCase caseData = caseRepo.save(new TroubleCase(caseGroup.getCaseManagementSystem(), receipt, caseGroup.getCaseType(), creationDate, extras));
			caseIssueRepo.save(new CaseIssue(caseData, "OLD", now));
			if (0 == i.incrementAndGet() % FAKE_SNOOZE_INTERVAL) {
				snoozeRepo.save(new CaseSnooze(caseData, "Just Because", 2));
			}
		};
		values.forEachRemaining(r);
		LOG.info("Creating {} cases/issues", i.get());
	}
}
