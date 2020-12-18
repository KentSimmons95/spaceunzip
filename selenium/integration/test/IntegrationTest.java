/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.integration.test;

import it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils.RestHelper;

import org.testng.Reporter;
import org.testng.annotations.Test;

import java.io.IOException;


public class IntegrationTest extends IntegrationSuiteParentBase {

    @Test(description = "Test to see if the upload file functionality works")
    protected void testUploadFunctionality()throws IOException{
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for testUploadFunctionality @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        uploadZipFile(EXPECTED_MESSAGE, "windows-zip-small.zip", pageId);
    }

    @Test(description = "Upload  ibmtest.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void uploadIbmTest() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadIbmTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyPageResults(12, "ibmtest.zip");
    }

    @Test(description = "Run the test to see if number of attachments unzipped matches expected result", dependsOnMethods = "testUploadFunctionality")
    protected void testAttachments() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for testAttachments @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        uploadZipFile(EXPECTED_MESSAGE, "attachment-test-case.zip", pageId);
        String pageId = RestHelper.getPageId(baseApplicationUsername, baseApplicationPassword, SPACE_KEY, "attachment-test-case");
        checkNumberOfAttachment("9", pageId);
    }

    @Test(description = "Upload cyrillic.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void uploadCyrillic() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadCyrillic @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyPageResults(0, "cyrillic.zip");
    }

    @Test(description = "Upload broken.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void testBrokenZip() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadBrokenTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        uploadZipFile("Unzipping Failed", "broken.zip", pageId);
    }

    @Test(description = "Upload generated-test-resources.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void uploadGeneratedTestResources() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadGeneratedTestResources @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyPageResults(22, "generated-test-resources.zip");
    }

    @Test(description = "Upload windows-zip-medium.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void uploadWindowsMediumZip() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadWindowsMediumZip @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyPageResults(3, "windows-zip-medium.zip");
    }

    @Test(description = "Upload windows-zip-small.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void uploadWindowsSmallZip() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadWindowsSmallZip @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyPageResults(2, "windows-zip-small.zip");
    }

    @Test(description = "Upload ZipTest.zip file to Confluence", dependsOnMethods = "testUploadFunctionality")
    protected void uploadZipTest() throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for uploadZipTest @~~~~~~~~~%s", SUB_HEADING_YELLOW_COLOR, RESET_COLOR), true);
        verifyPageResults(5, "ZipTest.zip");
    }
}
