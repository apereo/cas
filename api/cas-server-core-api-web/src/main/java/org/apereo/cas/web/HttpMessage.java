package org.apereo.cas.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link HttpMessage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class HttpMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 2015460875654586133L;

    /**
     * The default asynchronous callbacks enabled.
     */
    private static final boolean DEFAULT_ASYNCHRONOUS_CALLBACKS_ENABLED = true;

    private final URL url;

    private final String message;

    /**
     * Whether this message should be sent in an asynchronous fashion.
     * Default is true.
     **/
    private final boolean asynchronous;

    private int responseCode;

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
     * Encodes the message in UTF-8 format in preparation to send.
     *
     * @param message Message to format and encode
     * @return The encoded message.
     */
    protected String formatOutputMessageInternal(final String message) {
        try {
            return URLEncoder.encode(message, StandardCharsets.UTF_8);
        } catch (final Exception e) {
            val msg = String.format("Unable to encode URL %s", message);
            LOGGER.warn(msg, e);
        }
        return message;
    }

    public String getMessage() {
        return formatOutputMessageInternal(this.message);
    }
}
