package gov.usds.case_issues.controllers;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import gov.usds.case_issues.test_util.FixtureDataInitializationService;
import gov.usds.case_issues.test_util.HsqlDbTruncator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ControllerTestBase {

	@Autowired
	protected FixtureDataInitializationService dataService;
	@Autowired
	protected HsqlDbTruncator truncator;
	@Autowired
	protected MockMvc mvc;
}