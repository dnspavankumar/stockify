package com.signalist.backend.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendBaseUrl = "http://localhost:3000";
    private final Session session = new Session();
    private final Finnhub finnhub = new Finnhub();
    private final Mail mail = new Mail();
    private final Jobs jobs = new Jobs();
    private final Cors cors = new Cors();

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public Session getSession() {
        return session;
    }

    public Finnhub getFinnhub() {
        return finnhub;
    }

    public Mail getMail() {
        return mail;
    }

    public Jobs getJobs() {
        return jobs;
    }

    public Cors getCors() {
        return cors;
    }

    public static class Session {
        private String cookieName = "signalist_session";
        private Duration ttl = Duration.ofDays(14);

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class Finnhub {
        private String baseUrl = "https://finnhub.io/api/v1";
        private String apiKey = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Mail {
        private boolean enabled;
        private String fromName = "Signalist";
        private String fromAddress = "noreply@signalist.local";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getFromAddress() {
            return fromAddress;
        }

        public void setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
        }
    }

    public static class Jobs {
        private boolean dailyDigestEnabled;
        private String dailyDigestCron = "0 0 12 * * *";

        public boolean isDailyDigestEnabled() {
            return dailyDigestEnabled;
        }

        public void setDailyDigestEnabled(boolean dailyDigestEnabled) {
            this.dailyDigestEnabled = dailyDigestEnabled;
        }

        public String getDailyDigestCron() {
            return dailyDigestCron;
        }

        public void setDailyDigestCron(String dailyDigestCron) {
            this.dailyDigestCron = dailyDigestCron;
        }
    }

    public static class Cors {
        private List<String> allowedOriginPatterns = new ArrayList<>(List.of("*"));

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }
    }
}
