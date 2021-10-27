package org.jasig.cas.util.http;

import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Abstraction for a message that is sent to an http endpoint.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class HttpMessage {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessage.class);
    
    /** The default asynchronous callbacks enabled. */
    private static final boolean DEFAULT_ASYNCHRONOUS_CALLBACKS_ENABLED = true;
    
    private final URL url;
    private final String message;
    
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
     * @param url the url to which the message will be sent.
     * @param message the message itself.
     */
    public HttpMessage(final URL url, final String message) {
        this(url, message, DEFAULT_ASYNCHRONOUS_CALLBACKS_ENABLED);
    }
    
    /**
     * Prepare the sender with a given url and the message to send.
     *
     * @param url the url to which the message will be sent.
     * @param message the message itself.
     * @param async whether the message should be sent asynchronously.
     */
    public HttpMessage(final URL url, final String message, final boolean async) {
        this.url = url;
        this.message = message;
        this.asynchronous = async;
    }
    
    protected boolean isAsynchronous() {
        return this.asynchronous;
    }

    protected final URL getUrl() {
        return this.url;
    }
    
    protected final String getMessage() {
        return this.formatOutputMessageInternal(this.message);
    }
    
    protected final String getContentType() {
        return this.contentType;
    }
    
    protected final void setContentType(final String type) {
        this.contentType = type;
    }
    
    /**
     * Encodes the message in UTF-8 format in preparation to send.
     * @param message Message to format and encode
     * @return The encoded message.
     */
    protected String formatOutputMessageInternal(final String message) {
        try {
            return URLEncoder.encode(message, "UTF-8"); 
        } catch (final UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage(), e);
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
                .toString();
    }
}
