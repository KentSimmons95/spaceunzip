/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;

import java.util.concurrent.TimeUnit;

public class CheckElementVisibility {

    private final static int SHORT_WAIT_TIME =  1;
    private final static int LONG_WAIT_TIME =  20;

    public static boolean elementPresentByXpath(WebDriver driver, String element) {
        try {
            driver.manage().timeouts().implicitlyWait(SHORT_WAIT_TIME, TimeUnit.SECONDS);
            driver.findElement(By.xpath(element));
            driver.manage().timeouts().implicitlyWait(LONG_WAIT_TIME, TimeUnit.SECONDS);
            Reporter.log(String.format("Element present = true where element xpath is %s", element), true);
            return true;
        } catch (NoSuchElementException ex) {
            driver.manage().timeouts().implicitlyWait(LONG_WAIT_TIME, TimeUnit.SECONDS);
            Reporter.log(String.format("Element present = false where element xpath is %s", element), true);
            return false;
        }
    }

    public static boolean elementPresentByCssSelector(WebDriver driver, String element) {
        try {
            driver.manage().timeouts().implicitlyWait(SHORT_WAIT_TIME, TimeUnit.SECONDS);
            driver.findElement(By.cssSelector(element));
            driver.manage().timeouts().implicitlyWait(LONG_WAIT_TIME, TimeUnit.SECONDS);
            Reporter.log(String.format("Element present = true where element cssSelector is %s", element), true);
            return true;
        } catch (NoSuchElementException ex) {
            driver.manage().timeouts().implicitlyWait(LONG_WAIT_TIME, TimeUnit.SECONDS);
            Reporter.log(String.format("Element present = false where element cssSelector is %s", element), true);
            return false;
        }
    }

    public static boolean elementPresentById(WebDriver driver, String element) {
        try {
            driver.manage().timeouts().implicitlyWait(SHORT_WAIT_TIME, TimeUnit.SECONDS);
            driver.findElement(By.id(element));
            driver.manage().timeouts().implicitlyWait(LONG_WAIT_TIME, TimeUnit.SECONDS);
            Reporter.log(String.format("Element present = true where element id is %s", element), true);
            return true;
        } catch (NoSuchElementException ex) {
            driver.manage().timeouts().implicitlyWait(LONG_WAIT_TIME, TimeUnit.SECONDS);
            Reporter.log(String.format("Element present = false where element id is %s", element), true);
            return false;
        }
    }
}
