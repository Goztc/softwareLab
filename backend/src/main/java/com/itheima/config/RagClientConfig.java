package com.itheima.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagClientConfig {
    
    private boolean enabled = true;
    private String baseUrl = "http://localhost:5000";
    private int timeoutSeconds = 30;
    private int connectionTimeout = 5000;
    private int readTimeout = 30000;
    private Retry retry = new Retry();
    
    @Bean
    public RestTemplate ragRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 配置超时设置
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(connectionTimeout);
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(readTimeout);
        restTemplate.setRequestFactory(factory);
        
        return restTemplate;
    }
    
    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public Retry getRetry() {
        return retry;
    }
    
    public void setRetry(Retry retry) {
        this.retry = retry;
    }
    
    public static class Retry {
        private int maxAttempts = 3;
        private long delay = 1000;
        
        public int getMaxAttempts() {
            return maxAttempts;
        }
        
        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
        
        public long getDelay() {
            return delay;
        }
        
        public void setDelay(long delay) {
            this.delay = delay;
        }
    }
}