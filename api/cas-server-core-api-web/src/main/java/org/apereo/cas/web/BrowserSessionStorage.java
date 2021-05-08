package org.apereo.cas.web;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link BrowserSessionStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface BrowserSessionStorage extends Serializable {
    /**
     * session storage key to track data.
     */
    String KEY_SESSION_STORAGE = "sessionStorage";

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
     */
    void setDestinationUrl(String url);
}
