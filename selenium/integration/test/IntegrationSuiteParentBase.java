/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.integration.test;

import it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils.CheckElementVisibility;
import it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils.ParentSuiteBase;
import it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils.RestHelper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.IOException;

public class IntegrationSuiteParentBase extends ParentSuiteBase {

    final String SPACE_KEY = "TESTSPACE";
    final String EXPECTED_MESSAGE = "Unzipping Succeeded";
    String pageId;

    @BeforeClass(alwaysRun = true)
    public void closeNotification() {
        if (CheckElementVisibility.elementPresentByCssSelector(driver, "[class*='aui-message']")) {
            int totalNumberOfNotifications = driver.findElements(By.cssSelector("[class*='aui-message']")).size();
            for (int i = 1; i <= totalNumberOfNotifications; i++) {
                driver.findElement(By.xpath(String.format("(//span[@class='aui-icon icon-close'])[%s]", i))).click();
            }
        }
        discardWelcomeToTheDashboardDialogueBox(driver);
    }

    @BeforeMethod(alwaysRun = true)
    protected void initialiseSpace() throws IOException, InterruptedException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for IntegrationSuiteParentBase BeforeMethod method @~~~~~~~~~%s", HEADING_CYAN_COLOR, RESET_COLOR), true);
        if (RestHelper.getSpace(baseApplicationUsername, baseApplicationPassword, SPACE_KEY)) {
            RestHelper.deleteSpace(baseApplicationUsername, baseApplicationPassword, SPACE_KEY, driver);

        } else {
            Reporter.log("No space was found, creating the new test space ", true);
        }
        RestHelper.createSpace(baseApplicationUsername, baseApplicationPassword, "test", SPACE_KEY, "description");
        this.pageId = RestHelper.getPageId(baseApplicationUsername, baseApplicationPassword, SPACE_KEY, "test Home");
    }

    @AfterMethod(alwaysRun = true)
    protected void cleanSpace() throws IOException, InterruptedException {
        RestHelper.deleteSpace(baseApplicationUsername, baseApplicationPassword, SPACE_KEY, driver);
    }

    public void verifyPageResults(int expectedValue, String zipName) throws IOException {
        uploadZipFile(EXPECTED_MESSAGE, zipName, pageId);
        int countedPages = RestHelper.countPages(baseApplicationUsername, baseApplicationPassword, SPACE_KEY);
        Assert.assertEquals(countedPages, expectedValue, String.format("Number of pages(%d) retrieved does not match the number of pages(%d) expected!", countedPages, expectedValue));
    }

    public void checkNumberOfAttachment(String expectedValue, String attachmentPageId) throws IOException {
        String actualValue = String.valueOf(RestHelper.countAttachments(baseApplicationUsername, baseApplicationPassword, SPACE_KEY, attachmentPageId));
        Assert.assertEquals(actualValue, expectedValue, String.format("Number of attachments(%s) retrieved does not match the number of pages(%s) expected!", actualValue, expectedValue));
    }

    boolean uploadZipFile(String expectedMessage, String fileName, String pageId) throws IOException {
        String actualMessage = null,
                failureMessage = "Unzip Failed, please review and try again";
        String attachmentURL = String.format("http://localhost:1990/confluence/pages/viewpageattachments.action?pageId=%s", pageId);
        WebDriverWait waitForLoading = new WebDriverWait(driver, 30);
        RestHelper.uploadAttachment(baseApplicationUsername, baseApplicationPassword, SPACE_KEY, fileName, pageId);
        Reporter.log("Upload file attachment using REST", true);
        driver.navigate().to(attachmentURL);
        Reporter.log(String.format("Navigate to the attachment page with page ID of: %s", pageId), true);
        driver.findElement(By.id("spaceunzip.webitem.process.link")).click();
        Reporter.log("Click the SpaceUnZip attachment link", true);
        driver.findElement(By.cssSelector("[class*='Button__StyledButton-sc']")).click();
        waitForLoading.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[class*=styled__Container-sc]")));
        boolean zipSuccessful = CheckElementVisibility.elementPresentByCssSelector(driver, "[class*=styled__Container-sc]");
        if (zipSuccessful) {
            actualMessage = driver.findElement(By.cssSelector("[class*=styled__Title-sc]")).getText();
            if (driver.findElements(By.cssSelector("[class*='styled__Actions-sc'] li")).size() > 1) {
                String detailedMessage = driver.findElement(By.cssSelector("[class*='styled__Description']")).getText();
                failureMessage = String.format("Test failed. %s. Please review!", detailedMessage);
            }
        }
        Assert.assertEquals(actualMessage, expectedMessage, failureMessage);
        return zipSuccessful;
    }

    public static void discardWelcomeToTheDashboardDialogueBox(WebDriver driver) {
        if (CheckElementVisibility.elementPresentByXpath(driver, "//footer[@class='aui-dialog2-footer']")) {
            driver.findElement(By.xpath("//button[@class='aui-button aui-button-link skip-onboarding']")).click();
        }
    }
}

