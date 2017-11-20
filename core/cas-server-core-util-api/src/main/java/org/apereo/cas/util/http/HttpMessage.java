package org.apereo.cas.util.http;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.net.URL;

/**
 * Abstraction for a message that is sent to an http endpoint.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class HttpMessage implements Serializable {
    private static final long serialVersionUID = 2015460875654586133L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessage.class);

    /**
     * The default asynchronous callbacks enabled.
     */
    private static final boolean DEFAULT_ASYNCHRONOUS_CALLBACKS_ENABLED = true;

    private final URL url;
    private final String message;
    private int responseCode;

    /**
     * Whether this message should be sent in an asynchronous fashion.
     * Default is true.
     **/
    private final boolean asynchronous;

    /**
     * The content type for this message once submitted.
     * Default is {@link MediaType#APPLICATION_FORM_URLENCODED}.
     **/
    private String contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE;

    /**
     * Prepare the sender with a given url and the message to send.
     *
     * @param url     the url to which the message will be sent.
     * @param message the message itself.
     */
    public HttpMessage(final URL url, final String message) {
        this(url, message, DEFAULT_ASYNCHRONOUS_CALLBACKS_ENABLED);
    }

    /**
     * Prepare the sender with a given url and the message to send.
     *
     * @param url     the url to which the message will be sent.
     * @param message the message itself.
     * @param async   whether the message should be sent asynchronously.
     */
    public HttpMessage(final URL url, final String message, final boolean async) {
        this.url = url;
        this.message = message;
        this.asynchronous = async;
    }

    public boolean isAsynchronous() {
        return this.asynchronous;
    }

    public URL getUrl() {
        return this.url;
    }

    public String getMessage() {
        return this.formatOutputMessageInternal(this.message);
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(final String type) {
        this.contentType = type;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    /**
     * Encodes the message in UTF-8 format in preparation to send.
     *
     * @param message Message to format and encode
     * @return The encoded message.
     */
    protected String formatOutputMessageInternal(final String message) {
        try {
            return EncodingUtils.urlEncode(message);
        } catch (final Exception e) {
            LOGGER.warn("Unable to encode URL", e);
        }
        return message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("url", this.url)
                .append("message", this.message)
                .append("asynchronous", this.asynchronous)
                .append("contentType", this.contentType)
                .append("responseCode", this.responseCode)
                .toString();
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }
}
