/*
 * Copyright (c) 2020. TechTime Initiative Group Limited. All rights reserved.
 *
 *  The contents of this file have been approved for use by the author as a representative sample of the results
 *  of their work performed while employed by TechTime Initiative Group Limited.
 *
 *  For all questions, please contact support@techtime.co.nz
 */

package it.org.techtime.confluence.plugins.spaceunzip.selenium.parent.suit.utils;

import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;


import javax.json.Json;
import javax.json.JsonObject;

import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RestHelper {

    private final static CloseableHttpClient CLIENT = HttpClients.createDefault();

    public static void createSpace(String baseApplicationUsername, String baseApplicationPassword, String spaceName, String spaceKey, String spaceDescription) throws IOException {
        String requestUrl = "http://localhost:1990/confluence/rest/api/space/";
        URL url = new URL(requestUrl);
        HttpHost httpHost = new HttpHost(url.getHost(), url.getPort());
        HttpPost httpPost = new HttpPost(requestUrl);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(httpHost), new UsernamePasswordCredentials(baseApplicationUsername, baseApplicationPassword));
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        AuthCache authCache = new BasicAuthCache();
        authCache.put(httpHost, new BasicScheme());
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);
        JsonObject json = Json.createObjectBuilder()
                .add("key", spaceKey)
                .add("name", spaceName)
                .add("description", Json.createObjectBuilder()
                        .add("plain", Json.createObjectBuilder()
                                .add("value", spaceDescription)
                                .add("representation", "plain"))).build();
        StringEntity entity = new StringEntity(json.toString());
        httpPost.setEntity(entity);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        try (CloseableHttpResponse response = closeableHttpClient.execute(httpHost, httpPost, localContext)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reporter.log(String.format("Space created successfully where spaceName = %s and spaceKey = %s ", spaceName, spaceKey), true);
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                Reporter.log(String.format("Failed to create a new space where spaceName = %s, spaceKey = %s, and responseBody = %s", spaceName, spaceKey, responseBody), true);
                throw new IllegalStateException("Unable to create space, please try again with different configurations");
            }
        }
    }

    public static void deleteSpace(String baseApplicationUsername, String baseApplicationPassword, String spaceKey, WebDriver driver) throws IOException {
        String requestUrl = String.format("http://localhost:1990/confluence/rest/api/space/%s", spaceKey);
        HttpDelete httpDelete = new HttpDelete(requestUrl);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpDelete.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (CloseableHttpResponse response = CLIENT.execute(httpDelete)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                Reporter.log("The space has successfully been deleted: ", true);
                WebDriverWait wait = new WebDriverWait(driver, 30, 2000);
                wait.until(d -> {
                    try {
                        return !getSpace(baseApplicationUsername, baseApplicationPassword, "TESTSPACE");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
            } else {
                Reporter.log(String.format("%s could not be deleted, response code: %d", spaceKey, responseCode), true);
                throw new IllegalStateException(String.format("Unable to delete %s please try again with different configurations", spaceKey));
            }
        }
    }

    public static boolean getSpace(String baseApplicationUsername, String baseApplicationPassword, String spaceKey) throws IOException {
        String requestUrl = String.format("http://localhost:1990/confluence/rest/api/space/%s", spaceKey);
        HttpGet httpGet = new HttpGet(requestUrl);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (CloseableHttpResponse response = CLIENT.execute(httpGet)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reporter.log(String.format("The %s space exists", spaceKey), true);
                return true;
            } else {
                Reporter.log(String.format("There is no space currently called: %s, Response code: %s", spaceKey, responseCode), true);
                return false;
            }
        }
    }

    public static String getPageId(String baseApplicationUsername, String baseApplicationPassword, String spaceKey, String pageTitle) throws IOException {
        String title = encodeValue(pageTitle);
        String requestURL = String.format("http://localhost:1990/confluence/rest/api/content?title=%s&spaceKey=%s", title, spaceKey);
        HttpGet httpGet = new HttpGet(requestURL);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (CloseableHttpResponse response = CLIENT.execute(httpGet)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Reporter.log(String.format("Cannot find page with name %s, Response code is: %s ", pageTitle, responseCode), true);
                return null;
            }
            System.out.println("THE CLIENT IS: " + CLIENT.toString());
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            JSONObject result = jsonArray.getJSONObject(0);
            String pageId = result.getString("id");
            Reporter.log(String.format("Page found where page name = %s and pageId = %s ", pageTitle, pageId), true);
            return pageId;
        }
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static void uploadAttachment(String baseApplicationUsername, String baseApplicationPassword, String spaceKey, String zipName, String pageId) throws IOException {
        String requestURL = String.format("http://localhost:1990/confluence/rest/api/content/%s/child/attachment", pageId);
        HttpPost httpPost = new HttpPost(requestURL);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpPost.setHeader("X-Atlassian-Token", "nocheck");
        File zip = new File("src/test/resources/" + zipName);
        if (!zip.exists()) {
            Reporter.log("File does not exist", true);
            try {
                throw new Exception("File could not be found");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        HttpEntity entity = MultipartEntityBuilder.create().addPart("file", new FileBody(zip)).build();
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = CLIENT.execute(httpPost)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reporter.log(String.format("The attachment has been successfully attached to: %s", spaceKey), true);
            } else {
                Reporter.log(String.format("Cannot attach file to %s with status code: %d", spaceKey, responseCode), true);
            }
        }
    }

    public static int countPages(String baseApplicationUsername, String baseApplicationPassword, String spaceKey) throws IOException {
        String requestURL = String.format("http://localhost:1990/confluence/rest/api/space/%s/content", spaceKey);
        HttpGet httpGet = new HttpGet(requestURL);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (CloseableHttpResponse response = CLIENT.execute(httpGet)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reporter.log(String.format("Page structure has been found in space: %s", spaceKey), true);
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(responseBody);
                if (jsonObject.has("page")) {
                    jsonObject.getJSONObject("page");
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        Reporter.log(String.format("The number of child results that have been created is: %d", jsonArray.length() - 1), true);
                        return jsonArray.length() - 1;
                    } catch (JSONException jsonException) {
                        Reporter.log(String.format("'result' key not found in the response where response body is %s", responseBody), true);
                    }
                } else {
                    Reporter.log(String.format("'page' key not found in the response where response body is %s", responseBody), true);
                }
            } else {
                Reporter.log(String.format("No page structure was found in space : %s, Response code is: %s", spaceKey, responseCode), true);
                return 0;
            }
        }
        return 0;
    }

    public static int countAttachments(String baseApplicationUsername, String baseApplicationPassword, String spaceKey, String pageId) throws IOException {
        String requestURL = String.format("http://localhost:1990/confluence/rest/api/content/%s/child/attachment", pageId);
        HttpGet httpGet = new HttpGet(requestURL);
        String credentials = Base64.getEncoder().encodeToString(String.format("%s:%s", baseApplicationUsername, baseApplicationPassword).getBytes());
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + credentials);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try (CloseableHttpResponse response = CLIENT.execute(httpGet)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reporter.log(String.format("Page structure has been found in space: %s", spaceKey), true);
                String responseBody = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(responseBody);
                if (jsonObject.has("results")) {
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("results");
                        int length = jsonArray.length();
                        Reporter.log(String.format("There are %d attachments in the current page", length), true);
                        return length;
                    } catch (JSONException jsonException) {
                        Reporter.log(String.format("'result' key not found in the response where response body is %s", responseBody), true);
                    }
                } else {
                    Reporter.log(String.format("'results' key not found in the response where response body is %s", responseBody), true);
                }
            } else {
                Reporter.log(String.format("No attachments in the page found in space: %s, Response code is: %s", spaceKey, responseCode), true);
                return 0;
            }
        }
        return 0;
    }
}
