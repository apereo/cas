package org.apereo.cas.configuration.model.core.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for message.bundle.*
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "message.bundle", ignoreUnknownFields = false)
public class MessageBundleProperties {

    private String encoding = "UTF-8";

    private int cacheSeconds = 180;

    private boolean fallbackSystemLocale = false;

    private boolean useCodeMessage = true;

    private String[] baseNames = new String[] {"classpath:custom_messages", "classpath:messages"};

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public int getCacheSeconds() {
        return cacheSeconds;
    }

    public void setCacheSeconds(final int cacheSeconds) {
        this.cacheSeconds = cacheSeconds;
    }

    public boolean isFallbackSystemLocale() {
        return fallbackSystemLocale;
    }

    public void setFallbackSystemLocale(final boolean fallbackSystemLocale) {
        this.fallbackSystemLocale = fallbackSystemLocale;
    }

    public boolean isUseCodeMessage() {
        return useCodeMessage;
    }

    public void setUseCodeMessage(final boolean useCodeMessage) {
        this.useCodeMessage = useCodeMessage;
    }

    public String[] getBaseNames() {
        return baseNames;
    }

    public void setBaseNames(final String[] baseNames) {
        this.baseNames = baseNames;
    }
}
