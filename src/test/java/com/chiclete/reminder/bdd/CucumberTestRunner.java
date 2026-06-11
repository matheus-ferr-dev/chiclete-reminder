package com.chiclete.reminder.bdd;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectFile("cenarios-bdd.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.chiclete.reminder.bdd")
public class CucumberTestRunner {
}
