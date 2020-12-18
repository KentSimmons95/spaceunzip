/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebDriverHelper {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    protected static WebDriver getDriver() {
        return driver.get();
    }

    static WebDriver invokeBrowserAndReturnDriver(String browser, int pageLoadTimeout, boolean headless) {
        String operatingSystem = System.getProperty("os.name").toUpperCase();
        Reporter.log(String.format("Get operatingSystem name where operatingSystem = %s", operatingSystem), true);
        switch (browser.toLowerCase()) {
            case "firefox":
                Reporter.log("Kill Firefox driver", true);
                killDriver(operatingSystem, "taskkill /f /im geckodriver.exe", "pkill geckodriver");
                Reporter.log("Initialise WebDriver with Firefox driver", true);
                WebDriverManager.firefoxdriver().setup();
                driver.set(new FirefoxDriver());
                break;

            case "ie":
                if (StringUtils.containsIgnoreCase(operatingSystem, "windows")) {
                    try {
                        Reporter.log("Kill IE driver on Windows OS", true);
                        Runtime.getRuntime().exec("taskkill /f /im IEDriverServer.exe");
                    } catch (IOException ex) {
                        throw new IllegalStateException(String.format("Failed to kill IE driver. Review kill process and try again! Stack trace is: %s", ex));
                    }
                    Reporter.log("Initialise WebDriver with IE driver on Windows OS", true);
                    WebDriverManager.iedriver().setup();
                    driver.set(new InternetExplorerDriver());
                } else {
                    throw new IllegalStateException(String.format("You appear to be running %s operating system." +
                            " The IE driver only runs on Windows operating system. Tests not executed.", operatingSystem));
                }
                break;

            case "chrome":
                Reporter.log("Kill Chrome driver", true);
                killDriver(operatingSystem, "taskkill /f /im chromedriver.exe", "pkill chromedriver");
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    Reporter.log("Launch Chrome in headless mode", true);
                    options.addArguments(
                            "--start-maximised", "--window-size=1920,1080", "--disable-extensions", "--disable-gpu", "--no-sandbox",
                            "--js-flags=\"--max_old_space_size=200\"", "--ignore-certificate-errors", "--headless");
                }
                Reporter.log("Initialise WebDriver with Chrome driver", true);
                WebDriverManager.chromedriver().setup();
                driver.set(new ChromeDriver(options));
                break;

            default:
                throw new IllegalStateException("Invalid browser name provided, please enter either ie, firefox or chrome");
        }
        WebDriver driver = getDriver();
        setTestEnvironment(driver, pageLoadTimeout);
        return driver;
    }

    static void closeBrowser(WebDriver driver) {
        if (driver != null) {
            Reporter.log("Quit driver", true);
            driver.quit();
        } else {
            Reporter.log("Failed to quit driver. Please review!");
        }
    }

    static void setTestEnvironment(WebDriver driver, int pageLoadTimeout) {
        Reporter.log(String.format("Set implicit wait and page load timeout to %s seconds", pageLoadTimeout), true);
        driver.manage().deleteAllCookies();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(pageLoadTimeout, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
    }

    static void login(WebDriver driver, String loginPageUrl, String username, String password) {
        Reporter.log(String.format("Navigate to login page where loginPageUrl is %s", loginPageUrl), true);
        driver.navigate().to(loginPageUrl);
        Reporter.log(String.format("Send Username where username is '%s'", username), true);
        driver.findElement(By.xpath("//input[@name='os_username']")).sendKeys(username);
        Reporter.log("Send Password", true);
        driver.findElement(By.xpath("//input[@name='os_password']")).sendKeys(password);
        Reporter.log("Click login button", true);
        driver.findElement(By.xpath("//input[@name='login']")).click();
        Reporter.log("Wait for User Menu link to appear", true);
        Wait<WebDriver> waitForUserMenuLinkToAppear = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(10)).pollingEvery(Duration.ofSeconds(1));
        waitForUserMenuLinkToAppear.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(String.format("//a[@data-username='%s']", username))));
    }

    private static void killDriver(String operatingSystem, String killDriverOnWindows, String killDriverOnAnyOtherOS) {
        String killCommand = null;
        try {
            if (StringUtils.containsIgnoreCase(operatingSystem, "windows")) {
                Reporter.log("Kill driver on Windows OS", true);
                killCommand = killDriverOnWindows;
            } else {
                Reporter.log("Kill driver on Mac OS", true);
                killCommand = killDriverOnAnyOtherOS;
            }
            Runtime.getRuntime().exec(killCommand);
        } catch (IOException ex) {
            Reporter.log(String.format("Failed to kill driver. Command used is: %s", killCommand), true);
            throw new IllegalStateException(String.format("Review kill process and try again! Stack trace is: %s", ex));
        }
    }
}
