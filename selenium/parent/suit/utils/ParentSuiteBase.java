/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;
import org.testng.annotations.*;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ParentSuiteBase {

    /**
     * APPLICATION_PLATFORM: Whether the type of deployment of the host application is "Server" or "Data Center"
     * PLUGIN_LICENSE_PLATFORM: Whether the plugin license is for "Server" or "Data Center"
     * BASE_APPLICATION: Whether the host application is Jira, Confluence or Bitbucket
     * BASE_PRODUCT_NAME = Jira Software, Jira Service Desk, Jira Core
     */

    protected WebDriver driver;
    protected String browser;
    protected int defaultTimeOut;
    protected boolean headless;
    protected String spaceUnzipAppKey = "org.techtime.confluence.plugins.spaceunzip";
    protected final String TECHTIME_CORE_APP_KEY = "org.techtime.plugins.techtimecore";
    protected String baseApplicationUsername;
    protected String baseApplicationPassword;
    protected boolean appLicensed = false;
    protected int POLLING_WAIT = 1;
    protected int LONG_WAIT = 10;
//    protected int SHORT_WAIT = 5;
    protected static final String SUB_HEADING_YELLOW_COLOR = "\u001B[33m";
    protected static final String RESET_COLOR = "\033[0m";
    protected static final String HEADING_CYAN_COLOR = "\033[0;96m";
    protected final String APP_NAME = "Space Unzip";
    private final String HEADLESS = "headless";
    private final String HEADLESS_VALUE = "false";
    private final String BASE_APPLICATION_URL = "baseApplicationUrl";
    private final String LOGIN_URL = "http://localhost:1990/confluence";
//    private final String APPLICATION_PLATFORM = "applicationPlatform";
//    private final String APPLICATION_PLATFORM_VALUE = "Server";
//    private final String PLUGIN_LICENSE_PLATFORM = "pluginLicensePlatform"; // for future use
//    private final String PLUGIN_LICENSE_PLATFORM_VALUE = "Server";
//    private final String BASE_PRODUCT_NAME = "baseProductName";
//    private final String BASE_PRODUCT_NAME_VALUE = "Jira Service Desk";
    private final String BROWSER = "browser";
    private final String BROWSER_VALUE = "chrome";
    private final String USERNAME = "username";
    private final String USERNAME_VALUE = "admin";
    private final String PASSWORD = "password";
    private final String PASSWORD_VALUE = "admin";
    private final String SYNC_LOGIN = "syncLogin";
    private final String SYNC_LOGIN_VALUE = "//a[@aria-haspopup='true' and contains(@title,'" + USERNAME_VALUE + "')]";
    private final String DEFAULT_TIMEOUT = "defaultTimeout";
    private final String DEFAULT_TIMEOUT_VALUE = "30";
    private final String LICENSE_KEY = "licenseKey";
    private final String LICENSE_KEY_VALUE = "AAABCA0ODAoPeNpdj01PwkAURffzKyZxZ1IyUzARkllQ24gRaQMtGnaP8VEmtjPNfFT59yJVFyzfu\n" +
            "bkn796Ux0Bz6SmbUM5nbDzj97RISxozHpMUnbSq88poUaLztFEStUN6MJZ2TaiVpu/YY2M6tI6sQ\n" +
            "rtHmx8qd74EZ+TBIvyUU/AoYs7jiE0jzknWQxMuifA2IBlUbnQ7AulVjwN9AaU9atASs69O2dNFU\n" +
            "4wXJLc1aOUGw9w34JwCTTZoe7RPqUgep2X0Vm0n0fNut4gSxl/Jcnj9nFb6Q5tP/Ueu3L+0PHW4g\n" +
            "hZFmm2zZV5k6/95CbR7Y9bYGo/zGrV3Ir4jRbDyCA6vt34DO8p3SDAsAhQnJjLD5k9Fr3uaIzkXK\n" +
            "f83o5vDdQIUe4XequNCC3D+9ht9ZYhNZFKmnhc=X02dh";

    @BeforeSuite(alwaysRun = true)
    @Parameters({BROWSER, BASE_APPLICATION_URL, DEFAULT_TIMEOUT, USERNAME, PASSWORD, SYNC_LOGIN, LICENSE_KEY, HEADLESS})
    protected void loginAndApplyLicense(@Optional(BROWSER_VALUE) String browser, @Optional(LOGIN_URL) String baseApplicationUrl,
                                        @Optional(DEFAULT_TIMEOUT_VALUE) int defaultTimeout, @Optional(USERNAME_VALUE) String username,
                                        @Optional(PASSWORD_VALUE) String password, @Optional(SYNC_LOGIN_VALUE) String syncLogin,
                                        @Optional(LICENSE_KEY_VALUE) String pluginLicense, @Optional(HEADLESS_VALUE) boolean headless) throws IOException {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for ParentSuiteBase BeforeSuite method @~~~~~~~~~%s", HEADING_CYAN_COLOR, RESET_COLOR), true);
        this.baseApplicationUsername = username;
        Reporter.log(String.format("Initialise baseApplicationUsername where baseApplicationUsername = %s", this.baseApplicationUsername), true);
        Reporter.log("Initialise baseApplicationPassword", true);
        this.baseApplicationPassword = password;
        Reporter.log("Initialise WebDriver", true);
        this.driver = WebDriverHelper.invokeBrowserAndReturnDriver(browser, defaultTimeout, headless);
        WebDriverHelper.login(this.driver, baseApplicationUrl, username, password);
        this.browser = browser;
        Reporter.log(String.format("Initialise browser where browser = %s", this.browser), true);
        this.defaultTimeOut = defaultTimeout;
        Reporter.log(String.format("Initialise defaultTimeOut where defaultTimeOut = %s", this.defaultTimeOut), true);
        this.headless = headless;
        Reporter.log(String.format("Initialise headless where headless = %s", this.headless), true);
        applyLicense(baseApplicationUrl, this.baseApplicationUsername, this.baseApplicationPassword, pluginLicense);
    }

    @AfterSuite(alwaysRun = true)
    protected void quitDriver() {
        Reporter.log(String.format("%s~~~~~~~~~@ Printing logs for ParentSuiteBase AfterSuite method @~~~~~~~~~%s", HEADING_CYAN_COLOR, RESET_COLOR), true);
        WebDriverHelper.closeBrowser(driver);
    }

    private void applyLicense(String baseApplicationUrl, String baseApplicationUsername, String baseApplicationPassword, String license) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        String requestUrl = String.format("%s/rest/plugins/1.0/%s-key/license", baseApplicationUrl, spaceUnzipAppKey);
        HttpPut httpPut = new HttpPut(requestUrl);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        JsonObject json = Json.createObjectBuilder()
                .add("rawLicense", license).build();
        StringEntity entity = new StringEntity(json.toString());
        httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpPut.setEntity(entity);
        httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.atl.plugins+json");
        try (CloseableHttpResponse response = client.execute(httpPut)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reporter.log("License applied successfully", true);
                appLicensed = true;
            } else {
                String responseBody = EntityUtils.toString(response.getEntity(), UTF_8);
                if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST && responseBody.contains("upm.plugin.license.error.invalid.update")) {
                    Reporter.log(String.format("App is already licensed where response code is %s and response body is %s", responseCode, responseBody), true);
                    appLicensed = true;
                } else {
                    Reporter.log(String.format("Failed to apply license where response code is %s", responseCode), true);
                    throw new IllegalStateException("App not licensed. Please review and try again!");
                }
            }
        }
    }

}
