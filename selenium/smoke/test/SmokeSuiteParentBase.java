/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.smoke.test;

import it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils.CheckElementVisibility;
import it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils.ParentSuiteBase;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.util.Strings;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

public class SmokeSuiteParentBase extends ParentSuiteBase {

    private final MavenXpp3Reader reader = new MavenXpp3Reader();

    @BeforeClass(alwaysRun = true)
    protected void navigateToUpmAndInitialiseAppKey() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for SmokeSuiteParentBase BeforeSuite method @~~~~~~~~~%s", HEADING_CYAN_COLOR, RESET_COLOR), true);
        if (appLicensed) {
            String manageAllAppsUrl = "http://localhost:1990/confluence/plugins/servlet/upm/manage/all";
            Reporter.log(String.format("Navigate to Manage All Apps Page where navigation url is: %s", manageAllAppsUrl), true);
            driver.navigate().to(manageAllAppsUrl);
            WebDriverWait waitForLoading = new WebDriverWait(driver, LONG_WAIT);
            Reporter.log("Wait for Manage Apps panel to load", true);
            waitForLoading.until(ExpectedConditions.visibilityOfElementLocated(By.id("upm-manage-plugins-user-installed"))); // Manage Apps panel
            Reporter.log("Wait for Upload Apps link to appear", true);
            waitForLoading.until(ExpectedConditions.visibilityOfElementLocated(By.id("upm-manage-plugins-system"))); //Upload App link
        } else {
            Reporter.log("Failed to navigate to UPM where appLicensed = false", true);
        }
    }

    protected void verifyInstall(String appKey) {
        boolean appInstalledSuccessfully = false;
        String pluginList = "//div[@class='upm-manage-plugin-list']";
        Reporter.log("Check if any apps are installed or not", true);
        boolean anyAppsInstalled = CheckElementVisibility.elementPresentByXpath(driver, pluginList);
        if (anyAppsInstalled) {
            String appPanel = String.format("//div[@data-key='%s']", appKey); // This panel hold all details of the app
            Reporter.log(String.format("Check if app panel with app key(%s) exist or not where app panel xpath is %s", appKey, appPanel), true);
            appInstalledSuccessfully = CheckElementVisibility.elementPresentByXpath(driver, appPanel);
        }
        Reporter.log(String.format("%s%s installed successfully = %s where App key is %s%s", SUB_HEADING_YELLOW_COLOR, getAppName(appKey), appInstalledSuccessfully, appKey, RESET_COLOR), true);

        Assert.assertTrue(anyAppsInstalled && appInstalledSuccessfully);
    }

    protected void verifyAppStatus(String appKey) {
        boolean appEnabled = false;
        String appName = getAppName(appKey);
        String expectedButtonActionStatus = "disable";
        Reporter.log("Check if app settings are visible", true);
        expandAppSettings(appKey, appName);
        int totalNumberOfButtons = driver.findElements(By.xpath(String.format("//div[@data-key='%s']//a[@data-action]", appKey))).size();

        // Select right button to get 'data-action' because enabling plugin will increase the number of buttons which will give you wrong 'action-data'
        String enableDisableButtonXpath = String.format("(//div[@data-key='%s']//a[@data-action])[%s]", appKey, totalNumberOfButtons);
        Reporter.log(String.format("Get data-action status from Enable/Disable button where button xpath is %s", enableDisableButtonXpath), true);
        String actualButtonActionStatus = driver.findElement(By.xpath(enableDisableButtonXpath)).getAttribute("data-action");
        Reporter.log("Get total number of modules from UI", true);
        String appModules = driver.findElement(By.xpath("//span[contains(@class,'upm-count-enabled')]")).getText(); // returns you the whole String of Module message
        int actualNumberOfModulesEnabled = Integer.parseInt(appModules.split(" ")[0]); // returns you the specific number of enabled modules
        Reporter.log(String.format("Check if actual data-action status is same as expected where actual status is %s and expected is %s", actualButtonActionStatus, expectedButtonActionStatus), true);
        boolean enableDisableButtonStatus = StringUtils.containsIgnoreCase(actualButtonActionStatus, expectedButtonActionStatus);
        Reporter.log(String.format("Check if total number of enabled modules are greater than 0 where actual number of enabled modules are %s", actualNumberOfModulesEnabled), true);
        boolean modulesEnabled = actualNumberOfModulesEnabled > 0;
        if (enableDisableButtonStatus && modulesEnabled) {
            appEnabled = true;
        }
        Reporter.log(String.format("%s%s enabled = %s where App key is %s%s", SUB_HEADING_YELLOW_COLOR, appName, appEnabled, appKey, RESET_COLOR), true);

        Assert.assertTrue(appEnabled);
    }

    protected void verifyLicense(String appKey) {
        String appName = getAppName(appKey);
        String expectedLicenseStatus = "valid";
        String licenseStatus, failMessage, license;
        expandAppSettings(appKey, appName);
        Reporter.log("Get license key from License key text box", true);
        Wait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(LONG_WAIT)).pollingEvery(Duration.ofSeconds(POLLING_WAIT));
        String licenseKeyTextBoxValue = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format("//div[@data-key='%s']//div[@class='upm-plugin-license-key upm-plugin-detail']", appKey)))).getText();
        if (Strings.isNullOrEmpty(licenseKeyTextBoxValue)) {
            // fetching from License details text box
            license = String.format("//div[@data-key='%s']//div[@class='upm-plugin-license-info upm-plugin-detail']", appKey);
            boolean licenseTextBoxStatus = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(license))).isDisplayed();
            Reporter.log(String.format("Performed DOM check on licenseTextBox where xpath is %s and status is elementPresent = %s", license, licenseTextBoxStatus), true);
            Reporter.log(String.format("Fetch license status from License details text box where xpath is %s", license), true);
            licenseStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(license))).getText();
            failMessage = String.format("Test failed: %s not licensed where app key is %s. Please review!", appName, appKey);
        } else {
            // fetching from License status text box
            license = String.format("//div[@data-key='%s']//div[@class='upm-plugin-license-status upm-plugin-detail']", appKey);
            boolean licenseTextBoxStatus = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(license))).isDisplayed();
            Reporter.log(String.format("Performed DOM check on licenseTextBox where xpath is %s and status is elementPresent = %s", license, licenseTextBoxStatus), true);
            Reporter.log(String.format("Fetch license status from License status text box where xpath is %s", license), true);
            licenseStatus = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(license))).getText();
            failMessage = String.format("Test failed: %s has a expired license where app key is %s. Please review!", appName, appKey);
        }
        Reporter.log(String.format("%s%s license status is: %s where App key is %s%s", SUB_HEADING_YELLOW_COLOR, appName, licenseStatus, appKey, RESET_COLOR), true);

        Assert.assertEquals(licenseStatus.toLowerCase(), expectedLicenseStatus.toLowerCase(), failMessage);
    }

    protected void verifyAppVersion(String property, String appKey) throws IOException, XmlPullParserException {
        Model model = reader.read(new FileReader("pom.xml"));
        String appName = getAppName(appKey);
        expandAppSettings(appKey, appName);
        Reporter.log(String.format("Get property value from POM where property = %s", property), true);
        String expectedVersion = model.getProperties().getProperty(property);
        String version = String.format("//div[@data-key='%s']//div[@class='upm-plugin-installed-version upm-plugin-detail']", appKey);
        Reporter.log(String.format("Get license version from UI where xpath is %s and App key is: %s", version, appKey), true);
        String actualVersion = driver.findElement(By.xpath(version)).getText();
        Reporter.log(String.format("%sExpected app version is %s and Actual version found in UI is %s where App name is %s and App key is %s%s", SUB_HEADING_YELLOW_COLOR, expectedVersion, actualVersion, getAppName(appKey), appKey, RESET_COLOR), true);

        Assert.assertEquals(actualVersion, expectedVersion, "Test failed: Wrong version of App is installed. Please review!");
    }

    protected void verifyComponentVisibility(String id) {
        Reporter.log(String.format("Check if component is present or not  where component xpath is: %s", id), true);
        boolean componentVisible = CheckElementVisibility.elementPresentById(driver, id);

        Assert.assertTrue(componentVisible, "Test failed: Component not visible. Please review!");
    }

    private void expandAppSettings(String appKey, String appName) {
        Wait<WebDriver> waitForAppSettingsToLoad = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(LONG_WAIT)).pollingEvery(Duration.ofSeconds(POLLING_WAIT));
        String appDetails = String.format("//div[@data-key='%s']//div[@class='upm-details loaded']", appKey);
        if (!CheckElementVisibility.elementPresentByXpath(driver, appDetails)) {
            Reporter.log(String.format("Click on %s to expand settings where expander xpath is: %s", appName, appDetails), true);
            driver.findElement(By.xpath(String.format("//div[@data-key='%s']", appKey))).click(); // expand app settings
            WebElement expandSettings = driver.findElement(By.xpath(appDetails));
            Reporter.log("Wait for app settings to load", true);
            waitForAppSettingsToLoad.until(ExpectedConditions.visibilityOf(expandSettings));
        } else {
            Reporter.log(String.format("%s App settings already visible", appName), true);
        }
    }

    private String getAppName(String appKey) {
        String appPanel = String.format("[data-key='%s']", appKey); // This panel hold all details of the app
        Reporter.log(String.format("Get app name where appPanel css selector is %s", appPanel), true);
        String appName = driver.findElement(By.cssSelector(String.format("%s .upm-plugin-name", appPanel))).getText();
        Reporter.log(String.format("App name is %s", appName), true);
        return appName;
    }
}
