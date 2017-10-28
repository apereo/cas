package org.apereo.cas.configuration.model.core.web;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.CollectionUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Configuration properties class for message.bundle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class MessageBundleProperties implements Serializable {

    private static final long serialVersionUID = 3769733438559663237L;
    /**
     * Message bundle character encoding.
     */
    private String encoding = StandardCharsets.UTF_8.name();

    /**
     * Cache size.
     */
    private int cacheSeconds = 180;

    /**
     * Flag that controls whether to fallback to the default system locale if no locale is specified explicitly.
     */
    private boolean fallbackSystemLocale;

    /**
     * Flag that controls whether to use code message.
     */
    private boolean useCodeMessage = true;

    /**
     * A list of strings representing base names for this message bundle.
     */
    private List<String> baseNames = CollectionUtils.wrapList("classpath:custom_messages", "classpath:messages");

    /**
     * A list of strings representing common names for this message bundle.
     * <p>
     * Entries in last common names override first values (as opposed to baseNames used in message bundles).
     */
    private List<String> commonNames = CollectionUtils.wrapList("classpath:common_messages.properties", "file:/etc/cas/config/common_messages.properties");

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

    public List<String> getBaseNames() {
        return baseNames;
    }

    public void setBaseNames(final List<String> baseNames) {
        this.baseNames = baseNames;
    }

    public List<String> getCommonNames() {
        return commonNames;
    }

    public void setCommonNames(final List<String> commonNames) {
        this.commonNames = commonNames;
    }
}
