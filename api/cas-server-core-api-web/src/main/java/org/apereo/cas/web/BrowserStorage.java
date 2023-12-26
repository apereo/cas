package org.apereo.cas.web;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link BrowserStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface BrowserStorage extends Serializable {
    /**
     * session storage key to track data.
     */
    String PARAMETER_BROWSER_STORAGE = "browserStorage";
    
    /**
     * Gets payload.
     *
     * @return the payload
     */
    String getPayload();

    /**
     * Gets destination url.
     *
     * @return the destination url
     */
    String getDestinationUrl();

    /**
     * Sets destination url.
     *
     * @param url the url
     * @return the destination url
     */
    BrowserStorage setDestinationUrl(String url);

    /**
     * Sets context.
     *
     * @param context the context
     * @return the context
     */
    BrowserStorage setContext(String context);

    /**
     * Sets storage type.
     *
     * @param type the type
     * @return the storage type
     */
    BrowserStorage setStorageType(BrowserStorageTypes type);

    /**
     * Gets storage type.
     *
     * @return the storage type
     */
    BrowserStorageTypes getStorageType();
    
    /**
     * Sets remove on read.
     *
     * @param removeOnRead the remove on read
     * @return the remove on read
     */
    BrowserStorage setRemoveOnRead(boolean removeOnRead);

    enum BrowserStorageTypes {
        /**
         * Session storage.
         */
        SESSION,
        /**
         * Local storage.
         */
        LOCAL
    }
}
