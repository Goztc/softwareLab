package com.itheima.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通用HTTP请求工具类
 * <p>
 * 基于OkHttp实现的HTTP客户端工具，提供GET、POST等常用HTTP请求方法，
 * 支持自定义请求头、请求体、超时设置等。
 * </p>
 *
 * 使用示例：
 * <pre>
 * // GET请求示例
 * String getResponse = HttpClientUtil.getInstance()
 *     .url("https://api.example.com/data")
 *     .addHeader("Authorization", "Bearer token")
 *     .get()
 *     .executeForString();
 *
 * // POST JSON请求示例
 * Map<String, Object> requestBody = new HashMap<>();
 * requestBody.put("key", "value");
 * String postResponse = HttpClientUtil.getInstance()
 *     .url("https://api.example.com/api")
 *     .addHeader("Content-Type", "application/json")
 *     .jsonBody(requestBody)
 *     .post()
 *     .executeForString();
 *
 * // 添加查询参数
 * String response = HttpClientUtil.getInstance()
 *     .url("https://api.example.com/search")
 *     .addQueryParam("q", "keyword")
 *     .addQueryParam("page", "1")
 *     .get()
 *     .executeForString();
 *
 * // 表单提交
 * Map<String, String> formData = new HashMap<>();
 * formData.put("username", "user1");
 * formData.put("password", "pass123");
 * String response = HttpClientUtil.getInstance()
 *     .url("https://api.example.com/login")
 *     .formBody(formData)
 *     .post()
 *     .executeForString();
 *
 * // 直接获得 Json 回复
 * JSONObject jsonResponse = HttpClientUtil.getInstance()
 *     .url("https://api.example.com/data")
 *     .get()
 *     .executeForJson()
 * </pre>
 */
@Slf4j
@Component
public class HttpClientUtil {
    private static volatile HttpClientUtil instance;
    private final OkHttpClient client;
    private Request.Builder requestBuilder;
    private HttpUrl.Builder urlBuilder;
    private RequestBody requestBody;
    private Headers.Builder headersBuilder;

    /**
     * 私有构造函数，初始化OkHttpClient
     */
    private HttpClientUtil() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取HttpClientUtil单例实例
     *
     * @return HttpClientUtil实例
     */
    public static HttpClientUtil getInstance() {
        if (instance == null) {
            synchronized (HttpClientUtil.class) {
                if (instance == null) {
                    instance = new HttpClientUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 设置请求URL
     *
     * @param url 请求URL
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil url(String url) {
        this.urlBuilder = HttpUrl.parse(url).newBuilder();
        this.requestBuilder = new Request.Builder();
        this.headersBuilder = new Headers.Builder();
        return this;
    }

    /**
     * 添加URL查询参数
     *
     * @param name  参数名
     * @param value 参数值
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil addQueryParam(String name, String value) {
        if (urlBuilder != null) {
            urlBuilder.addQueryParameter(name, value);
        }
        return this;
    }

    /**
     * 添加请求头
     *
     * @param name  头名称
     * @param value 头值
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil addHeader(String name, String value) {
        if (headersBuilder != null) {
            headersBuilder.add(name, value);
        }
        return this;
    }

    /**
     * 设置JSON格式的请求体
     *
     * @param body 请求体对象，会自动转换为JSON字符串
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil jsonBody(Object body) {
        this.requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                JSON.toJSONString(body)
        );
        return this;
    }

    /**
     * 设置表单格式的请求体
     *
     * @param formData 表单数据键值对
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil formBody(Map<String, String> formData) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formData.forEach(formBuilder::add);
        this.requestBody = formBuilder.build();
        return this;
    }

    /**
     * 设置自定义请求体
     *
     * @param requestBody 自定义请求体
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil body(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * 构建GET请求
     *
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil get() {
        buildRequest("GET");
        return this;
    }

    /**
     * 构建POST请求
     *
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil post() {
        buildRequest("POST");
        return this;
    }

    /**
     * 构建PUT请求
     *
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil put() {
        buildRequest("PUT");
        return this;
    }

    /**
     * 构建DELETE请求
     *
     * @return HttpClientUtil实例（支持链式调用）
     */
    public HttpClientUtil delete() {
        buildRequest("DELETE");
        return this;
    }

    /**
     * 执行请求并返回Response对象
     *
     * @return OkHttp Response对象
     * @throws IOException 如果请求执行失败
     */
    public Response execute() throws IOException {
        if (requestBuilder == null) {
            throw new IllegalStateException("Request not built. Call url() and a HTTP method first.");
        }

        logRequestDetails();
        return client.newCall(requestBuilder.build()).execute();
    }

    /**
     * 执行请求并返回响应体字符串
     *
     * @return 响应体字符串
     * @throws IOException 如果请求执行失败
     */
    public String executeForString() throws IOException {
        try (Response response = execute()) {
            return handleResponse(response);
        }
    }

    /**
     * 执行请求并返回JSONObject对象
     *
     * @return JSONObject对象
     * @throws IOException 如果请求执行失败
     */
    public JSONObject executeForJson() throws IOException {
        String responseString = executeForString();
        return JSON.parseObject(responseString);
    }

    /**
     * 构建请求
     *
     * @param method HTTP方法
     */
    private void buildRequest(String method) {
        if (urlBuilder == null) {
            throw new IllegalStateException("URL not set. Call url() first.");
        }

        requestBuilder.url(urlBuilder.build());

        if (headersBuilder != null) {
            requestBuilder.headers(headersBuilder.build());
        }

        if (requestBody != null && !"GET".equalsIgnoreCase(method)) {
            requestBuilder.method(method, requestBody);
        } else {
            requestBuilder.method(method, null);
        }
    }

    /**
     * 处理响应
     *
     * @param response OkHttp响应对象
     * @return 响应体字符串
     * @throws IOException 如果响应处理失败
     */
    private String handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected response code: " + response.code());
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("Response body is null");
        }

        String responseString = responseBody.string();
        log.info("API Response: {}", responseString);
        return responseString;
    }

    /**
     * 记录请求详细信息
     */
    private void logRequestDetails() {
        Request request = requestBuilder.build();
        log.info("Request Details:");
        log.info("Method: {}", request.method());
        log.info("URL: {}", request.url());
        log.info("Headers: {}", request.headers());
        if (request.body() != null) {
            log.info("Request body exists.");
        }
    }
}