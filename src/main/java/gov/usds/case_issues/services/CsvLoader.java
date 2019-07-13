package gov.usds.case_issues.services;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import gov.usds.case_issues.db.model.CaseIssue;
import gov.usds.case_issues.db.model.CaseManagementSystem;
import gov.usds.case_issues.db.model.CaseType;
import gov.usds.case_issues.db.model.TroubleCase;
import gov.usds.case_issues.db.repositories.CaseIssueRepository;
import gov.usds.case_issues.db.repositories.CaseManagementSystemRepository;
import gov.usds.case_issues.db.repositories.CaseTypeRepository;
import gov.usds.case_issues.db.repositories.TroubleCaseRepository;

@Service
@PropertySource(value="sample_data.properties")
@Profile({"dev","local"})
public class CsvLoader {

	private static final Logger LOG = LoggerFactory.getLogger(CsvLoader.class);

	private TroubleCaseRepository caseRepo;
	private CaseManagementSystemRepository caseManagerRepo;
	private CaseTypeRepository caseTypeRepo;
	private CaseIssueRepository caseIssueRepo;

	@Value("${sample_data.receipt_key}")
	public String receiptKey;
	@Value("${sample_data.case_creation_date_key}")
	public String creationDateKey;
	@Value("${sample_data.case_creation_date_format}")
	public String creationDateFormat;

	public CsvLoader(TroubleCaseRepository caseRepo, CaseManagementSystemRepository caseManagerRepo,
			CaseTypeRepository caseTypeRepo, CaseIssueRepository caseIssueRepo) {
		super();
		this.caseRepo = caseRepo;
		this.caseManagerRepo = caseManagerRepo;
		this.caseTypeRepo = caseTypeRepo;
		this.caseIssueRepo = caseIssueRepo;
	}

	@Transactional
	public <T extends Iterator<Map<String,String>>> void loadAll(T values) {
		CaseManagementSystem defaultSystem = caseManagerRepo.save(
				new CaseManagementSystem("DEFAULT", "Default System", "Case Management System for testing"));
		CaseType defaultType = caseTypeRepo.save(new CaseType("STANDARD", "Standard Case Type", "Case type for testing"));
		ZonedDateTime now = ZonedDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(creationDateFormat);
		AtomicInteger i = new AtomicInteger(0);
		Consumer<Map<String,String>> r = row -> {
			String receipt = row.get(receiptKey);
			ZonedDateTime creationDate = ZonedDateTime.parse(row.get(creationDateKey), formatter);
			TroubleCase caseData = caseRepo.save(new TroubleCase(defaultSystem, receipt, defaultType, creationDate));
			caseIssueRepo.save(new CaseIssue(caseData, "OLD", now));
			i.incrementAndGet();
		};
		values.forEachRemaining(r);
		LOG.info("Created {} cases/issues", i.get());
	}
}
