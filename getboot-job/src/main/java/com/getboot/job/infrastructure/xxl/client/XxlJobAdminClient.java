/*
 * Copyright (c) 2026 qiheng. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getboot.job.infrastructure.xxl.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * XXL-JOB 管理端客户端。
 *
 * <p>用于登录管理后台并执行任务新增、修改、查询等管理操作。</p>
 *
 * @author qiheng
 */
@Slf4j
public class XxlJobAdminClient {

    private String adminUrl;
    private String username;
    private String password;
    private String appName;
    private String cookie;

    public void initConfig(String adminUrl, String username, String password,String appName) {
        this.adminUrl = resolvePrimaryAdminUrl(adminUrl);
        this.username = username;
        this.password = password;
        this.appName = appName;
    }

    static String resolvePrimaryAdminUrl(String adminUrls) {
        if (adminUrls == null) {
            return null;
        }
        return Arrays.stream(adminUrls.split(","))
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .findFirst()
                .map(XxlJobAdminClient::stripTrailingSlash)
                .orElseGet(() -> stripTrailingSlash(adminUrls.trim()));
    }

    private static String stripTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 登录XXL-JOB管理后台（使用全局配置）
     */
    public void login()  {
        try {
            checkConfig();
            String loginUrl = adminUrl + "/login";

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(loginUrl);

                // 设置表单参数
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("userName", username));
                params.add(new BasicNameValuePair("password", password));
                httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

                // 执行请求
                HttpResponse response = httpClient.execute(httpPost);

                // 处理响应
                if (response.getStatusLine().getStatusCode() == 200) {
                    log.info("XXL-JOB login succeeded. response={}", response);
                    // 专门获取 XXL_JOB_LOGIN_IDENTITY cookie
                    Header[] cookieHeaders = response.getHeaders("Set-Cookie");
                    String loginCookie = null;

                    for (Header header : cookieHeaders) {
                        if (header.getValue().contains("XXL_JOB_LOGIN_IDENTITY")) {
                            // 提取完整的 cookie 字符串
                            loginCookie = header.getValue().split(";")[0];
                            break;
                        }
                    }

                    if (loginCookie != null) {
                        cookie = loginCookie;
                        log.info("Retrieved XXL-JOB login cookie successfully.");
                    } else {
                        log.error("XXL-JOB login succeeded but XXL_JOB_LOGIN_IDENTITY cookie was missing.");
                        throw new IOException("Missing required XXL-JOB login credential.");
                    }
                } else {
                    log.error("XXL-JOB login failed. status={}", response.getStatusLine().getStatusCode());
                    throw new IOException("XXL-JOB login failed. status=" + response.getStatusLine().getStatusCode());
                }
            }
        } catch (Exception e) {
            log.error("Unexpected XXL-JOB login error.", e);
            throw new IllegalStateException("XXL-JOB login failed.", e);
        }
    }

    /**
     * 新增任务（使用全局adminUrl）
     */
    public JSONObject addJob(JSONObject requestInfo) throws IOException {
        return doFormPost("/jobinfo/add", requestInfo);
    }

    /**
     * 更新任务（使用全局adminUrl）
     */
    public JSONObject updateJob(JSONObject requestInfo) throws IOException {
        return doFormPost("/jobinfo/update", requestInfo);
    }

    /**
     * 删除任务（使用全局adminUrl）
     */
    public JSONObject deleteJob(int id) throws IOException {
        return doGet("/jobinfo/delete?id=" + id);
    }

    /**
     * 开始任务（使用全局adminUrl）
     */
    public JSONObject startJob(int id) throws IOException {
        return doGet("/jobinfo/start?id=" + id);
    }

    /**
     * 停止任务（使用全局adminUrl）
     */
    public JSONObject stopJob(int id) throws IOException {
        return doGet("/jobinfo/stop?id=" + id);
    }

    /**
     * 根据执行器名称获取执行器ID
     */
    public  Long getJobGroupIdByAppname(String appname) throws IOException {
        checkConfig();

        // 构建查询参数
        String encodedAppname = URLEncoder.encode(appname, StandardCharsets.UTF_8);
        String path = "/jobgroup/pageList?appname=" + encodedAppname + "&start=0&length=100000";

        JSONObject result = doGet(path);

        if (result.getInteger("code") == 200) {
            // 获取分页数据
            JSONObject pageData = result.getJSONObject("data");
            if (pageData != null) {
                // 获取执行器列表
                JSONArray records = pageData.getJSONArray("data");
                if (records != null && !records.isEmpty()) {
                    // 获取第一个执行器对象
                    JSONObject executor = records.getJSONObject(0);
                    return executor.getLong("id");
                }
            }
        }

        log.error("Failed to get executor group ID. response={}", result);
        throw new RuntimeException("Executor not found: " + appname);
    }

    /**
     * 使用 Form Data 发送 POST 请求（支持 JSONObject 参数）
     */
    private JSONObject doFormPost(String path, JSONObject formData) throws IOException {
        checkConfig();
        ensureLogin();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(adminUrl + path);

            // 设置 Form Data 格式
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Cookie", cookie);

            // 构建 Form Data 参数
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 处理不同类型的值
                if (value != null) {
                    params.add(new BasicNameValuePair(key, value.toString()));
                } else {
                    params.add(new BasicNameValuePair(key, ""));
                }
            }

            // 设置请求体
            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            // 执行请求
            HttpResponse response = httpClient.execute(httpPost);
            return parseResponse(response);
        }
    }



    /**
     * 执行POST请求（使用全局adminUrl和cookie）
     */
    private JSONObject doPost(String path, JSONObject requestInfo) throws IOException {
        checkConfig();
        ensureLogin();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(adminUrl + path);

            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            httpPost.setHeader("Cookie", cookie);

            StringEntity entity = new StringEntity(requestInfo.toString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            return parseResponse(response);
        }
    }

    /**
     * 执行GET请求（使用全局adminUrl和cookie）
     */
    private JSONObject doGet(String path) throws IOException {
        checkConfig();
        ensureLogin();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(adminUrl + path);
            httpGet.setHeader("Cookie", cookie);

            HttpResponse response = httpClient.execute(httpGet);
            return parseResponse(response);
        }
    }

    /**
     * 解析HTTP响应
     */
    private JSONObject parseResponse(HttpResponse response) throws IOException {
        JSONObject result = new JSONObject();
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream ignored = entity.getContent()) {
                String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                if (response.getStatusLine().getStatusCode() == 200) {
                    result = JSONObject.parseObject(responseBody);
                } else {
                    result.put("error", responseBody);
                    log.error("Request failed. status={}, body={}",
                            response.getStatusLine().getStatusCode(), responseBody);
                }
            }
        } else {
            result.put("error", "Empty response entity");
            log.error("Response entity is empty.");
        }

        return result;
    }

    /**
     * 检查配置是否初始化
     */
    private void checkConfig() {
        if (isBlank(adminUrl) || isBlank(username) || isBlank(password) || isBlank(appName)) {
            throw new IllegalStateException("XXL-JOB configuration has not been initialized. Call initConfig first.");
        }
    }

    private void ensureLogin() {
        if (isBlank(cookie)) {
            login();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
