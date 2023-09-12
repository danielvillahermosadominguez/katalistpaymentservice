package com.codurance.katalyst.payment.application.acceptance;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/cucumber/features",
                 plugin = { "pretty",
                              "json:target/jsonReports/acceptance.json",
                              "html:target/cucumber/acceptance.html" })
public class CucumberAPITestRunner {

}
