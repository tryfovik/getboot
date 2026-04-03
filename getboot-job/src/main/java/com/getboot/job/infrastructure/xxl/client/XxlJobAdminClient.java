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

    /**
     * XXL-JOB 管理端基础地址。
     */
    private String adminUrl;

    /**
     * 管理端登录用户名。
     */
    private String username;

    /**
     * 管理端登录密码。
     */
    private String password;

    /**
     * 执行器应用名称。
     */
    private String appName;

    /**
     * 登录成功后缓存的 Cookie。
     */
    private String cookie;

    /**
     * 初始化管理端客户端配置。
     *
     * @param adminUrl 管理端地址
     * @param username 管理端登录用户名
     * @param password 管理端登录密码
     * @param appName 执行器应用名称
     */
    public void initConfig(String adminUrl, String username, String password, String appName) {
        this.adminUrl = resolvePrimaryAdminUrl(adminUrl);
        this.username = username;
        this.password = password;
        this.appName = appName;
    }

    /**
     * 从地址列表中解析主管理端地址。
     *
     * @param adminUrls 管理端地址列表
     * @return 主管理端地址
     */
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

    /**
     * 去掉地址末尾的斜杠。
     *
     * @param url 原始地址
     * @return 去掉末尾斜杠后的地址
     */
    private static String stripTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 登录 XXL-JOB 管理后台。
     */
    public void login() {
        try {
            checkConfig();
            String loginUrl = adminUrl + "/login";

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(loginUrl);

                // 构建登录表单参数。
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("userName", username));
                params.add(new BasicNameValuePair("password", password));
                httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

                // 执行登录请求。
                HttpResponse response = httpClient.execute(httpPost);

                // 处理登录响应。
                if (response.getStatusLine().getStatusCode() == 200) {
                    log.info("XXL-JOB login succeeded. response={}", response);
                    // 提取 XXL_JOB_LOGIN_IDENTITY 登录凭证。
                    Header[] cookieHeaders = response.getHeaders("Set-Cookie");
                    String loginCookie = null;

                    for (Header header : cookieHeaders) {
                        if (header.getValue().contains("XXL_JOB_LOGIN_IDENTITY")) {
                            // 提取完整 Cookie 字符串。
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
     * 新增任务。
     *
     * @param requestInfo 任务请求参数
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
     */
    public JSONObject addJob(JSONObject requestInfo) throws IOException {
        return doFormPost("/jobinfo/add", requestInfo);
    }

    /**
     * 更新任务。
     *
     * @param requestInfo 任务请求参数
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
     */
    public JSONObject updateJob(JSONObject requestInfo) throws IOException {
        return doFormPost("/jobinfo/update", requestInfo);
    }

    /**
     * 删除任务。
     *
     * @param id 任务 ID
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
     */
    public JSONObject deleteJob(int id) throws IOException {
        return doGet("/jobinfo/delete?id=" + id);
    }

    /**
     * 启动任务。
     *
     * @param id 任务 ID
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
     */
    public JSONObject startJob(int id) throws IOException {
        return doGet("/jobinfo/start?id=" + id);
    }

    /**
     * 停止任务。
     *
     * @param id 任务 ID
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
     */
    public JSONObject stopJob(int id) throws IOException {
        return doGet("/jobinfo/stop?id=" + id);
    }

    /**
     * 根据执行器应用名获取执行器分组 ID。
     *
     * @param appname 执行器应用名
     * @return 执行器分组 ID
     * @throws IOException 请求失败时抛出
     */
    public Long getJobGroupIdByAppname(String appname) throws IOException {
        checkConfig();

        // 构建查询参数。
        String encodedAppname = URLEncoder.encode(appname, StandardCharsets.UTF_8);
        String path = "/jobgroup/pageList?appname=" + encodedAppname + "&start=0&length=100000";

        JSONObject result = doGet(path);

        if (result.getInteger("code") == 200) {
            // 读取分页响应中的执行器列表。
            JSONObject pageData = result.getJSONObject("data");
            if (pageData != null) {
                JSONArray records = pageData.getJSONArray("data");
                if (records != null && !records.isEmpty()) {
                    JSONObject executor = records.getJSONObject(0);
                    return executor.getLong("id");
                }
            }
        }

        log.error("Failed to get executor group ID. response={}", result);
        throw new RuntimeException("Executor not found: " + appname);
    }

    /**
     * 以 Form Data 方式发送 POST 请求。
     *
     * @param path 请求路径
     * @param formData 表单数据
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
     */
    private JSONObject doFormPost(String path, JSONObject formData) throws IOException {
        checkConfig();
        ensureLogin();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(adminUrl + path);

            // 设置 Form Data 格式。
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Cookie", cookie);

            // 构建表单参数。
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 统一将参数转换为字符串。
                if (value != null) {
                    params.add(new BasicNameValuePair(key, value.toString()));
                } else {
                    params.add(new BasicNameValuePair(key, ""));
                }
            }

            // 设置请求体并执行请求。
            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            return parseResponse(response);
        }
    }

    /**
     * 以 JSON 方式发送 POST 请求。
     *
     * @param path 请求路径
     * @param requestInfo 请求体
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
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
     * 发送 GET 请求。
     *
     * @param path 请求路径
     * @return 管理端响应
     * @throws IOException 请求失败时抛出
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
     * 解析 HTTP 响应。
     *
     * @param response HTTP 响应
     * @return 解析后的 JSON 响应
     * @throws IOException 读取响应失败时抛出
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
     * 检查客户端配置是否已初始化。
     */
    private void checkConfig() {
        if (isBlank(adminUrl) || isBlank(username) || isBlank(password) || isBlank(appName)) {
            throw new IllegalStateException("XXL-JOB configuration has not been initialized. Call initConfig first.");
        }
    }

    /**
     * 确保当前已经完成登录。
     */
    private void ensureLogin() {
        if (isBlank(cookie)) {
            login();
        }
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 待判断字符串
     * @return 空白时返回 {@code true}
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
