/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.util.ArrayList;
import java.util.List;


public class ResultListener implements ITestListener {

    private boolean smokeTestsFailed = false;
    private static boolean getSuiteName = false;
    private static int numberOfFailedTests = 0;
    private static int numberOfPassedTests = 0;
    private static int numberOfSkippedTests = 0;
    private static String suiteName;
    private static int numberOfSuitesExecuted = 0;
    private List<String> failedTestList = new ArrayList<>();
    private List<String> skippedTestList = new ArrayList<>();

    /**
     * Invoked each time before a test will be invoked.
     * The <code>ITestResult</code> is only partially filled with the references to
     * class, method, start millis and status.
     *
     * @param result the partially filled <code>ITestResult</code>
     * @see ITestResult#STARTED
     */
    @Override
    public void onTestStart(ITestResult result) {
    }

    /**
     * Invoked each time a test succeeds.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SUCCESS
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        numberOfPassedTests++;
    }

    /**
     * Invoked each time a test fails.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#FAILURE
     */
    @Override
    public void onTestFailure(ITestResult result) {
        numberOfFailedTests++;
        failedTestList.add(result.getName());
        if (result.getTestContext().getSuite().getName().equalsIgnoreCase(suiteName)) {
            smokeTestsFailed = true;
        }
    }

    /**
     * Invoked each time a test is skipped.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SKIP
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        numberOfSkippedTests++;
        skippedTestList.add(result.getName());
    }

    /**
     * Invoked each time a method fails but has been annotated with
     * successPercentage and this failure still keeps it within the
     * success percentage requested.
     *
     * @param result <code>ITestResult</code> containing information about the run test
     * @see ITestResult#SUCCESS_PERCENTAGE_FAILURE
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    /**
     * Invoked after the test class is instantiated and before
     * any configuration method is called.
     *
     * Invoked each time a Suite is executed
     */
    @Override
    public void onStart(ITestContext context) {
        numberOfSuitesExecuted++;
        if (!getSuiteName) {
            suiteName = context.getSuite().getName();
            if (numberOfSuitesExecuted == 1) {
                getSuiteName = true;
            }
        }
    }

    /**
     * Invoked after all the tests have run and all their
     * Configuration methods have been called.
     *
     * Invoked each time a Suite is finished
     */
    @Override
    public void onFinish(ITestContext context) {
        if (smokeTestsFailed) {
            System.out.println("\n~~~~~~~~~~~~~~~~~~ PLEASE PAY ATTENTION HERE ~~~~~~~~~~~~~~~~~~\n");
            System.out.println(String.format(">> Please review '%s'. There are one or multiple test failures", suiteName.toUpperCase()));
            System.out.println(">> Total number of tests: -------- " + (numberOfFailedTests + numberOfPassedTests + numberOfSkippedTests));
            System.out.println(">> Number of tests executed: ----- " + (numberOfFailedTests + numberOfPassedTests));
            System.out.println(String.format(">> Number of FAILED tests: ------- %s", numberOfFailedTests));
            System.out.println(String.format(">> Number of PASSED tests: ------- %s", numberOfPassedTests));
            if (!failedTestList.isEmpty()) {
                int serialNumber = 0;
                System.out.println(">> List of FAILED Tests: ");
                for (String failedTestName : failedTestList) {
                    System.out.println(++serialNumber + ". " + failedTestName);
                }
            }
            if (!skippedTestList.isEmpty()) {
                int serialNumber = 0;
                System.out.println(">> List of SKIPPED Tests: ");
                for (String skippedTestName : skippedTestList) {
                    System.out.println(++serialNumber + ". " + skippedTestName);
                }
            } else {
                System.out.println("\n>> List of SKIPPED Tests: No tests skipped\n");
            }
            throw new SkipException(String.format("\n \n~~~~~~~~@ _ @~~~~~~~~ ATTENTION: ONE OR MULTIPLE TESTS FAILED IN %s. PLEASE REVIEW! ~~~~~~~~@ _ @~~~~~~~~\n", suiteName.toUpperCase()));
        }
    }
}
