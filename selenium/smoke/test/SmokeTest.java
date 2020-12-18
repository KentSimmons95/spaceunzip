/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.smoke.test;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.io.IOException;

public class SmokeTest extends SmokeSuiteParentBase {

    @Test(priority = 6, description = "Test verifies if TechTime Core is installed or not")
    private void coreInstallationTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for coreInstallationTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyInstall(TECHTIME_CORE_APP_KEY);
    }

    @Test(priority = 6, dependsOnMethods = "coreInstallationTest", description = "Test verifies if TechTime Core is enabled or not")
    private void coreEnableTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for coreEnableTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyAppStatus(TECHTIME_CORE_APP_KEY);
    }

    @Test(priority = 6, dependsOnMethods = "coreInstallationTest", description = "Test verifies if right version of Core is installed or not")
    private void coreVersionTest() throws IOException, XmlPullParserException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for coreVersionTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyAppVersion("techtime.core.version", TECHTIME_CORE_APP_KEY);
    }

    @Test(priority = 7, description = "Test verifies that if App is successfully installed or not")
    private void appInstallationTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for appInstallationTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyInstall(spaceUnzipAppKey);
    }

    @Test(priority = 7, dependsOnMethods = "appInstallationTest", description = "Test verifies if app is enabled or not")
    private void appEnableTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for appEnableTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyAppStatus(spaceUnzipAppKey);
    }

    @Test(priority = 7, dependsOnMethods = "appInstallationTest", description = "Test verifies if App is licensed or not")
    private void licenseVerificationTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for licenseVerificationTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyLicense(spaceUnzipAppKey);
    }

    @Test(priority = 8, description = "Test verifies if TechTime Apps Panel is visible or not")
    private void appsPanelVisibilityTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for appsPanelVisibilityTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyComponentVisibility("section-menuheading-techtime_addons_menu");
    }

    @Test(priority = 8, dependsOnMethods = "appsPanelVisibilityTest", description = "Under TechTime Apps Panel, test verifies if App link is present or not")
    private void appsPanelAppVisibilityTest() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for appsPanelAppVisibilityTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyComponentVisibility("spaceunzip.started.link");
    }
}
