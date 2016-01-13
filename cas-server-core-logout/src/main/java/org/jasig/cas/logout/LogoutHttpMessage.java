package org.jasig.cas.logout;

import org.jasig.cas.util.http.HttpMessage;
import org.springframework.http.MediaType;

import java.net.URL;

/**
 * A logout http message that is accompanied by a special content type
 * and formatting.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class LogoutHttpMessage extends HttpMessage {

    /** The parameter name that contains the logout request. */
    private static final String LOGOUT_PARAMETER_NAME = "logoutRequest";

    /**
     * Constructs a logout message.
     *
     * @param url The url to send the message to
     * @param message Message to send to the url
     */
    LogoutHttpMessage(final URL url, final String message, final boolean asynchronous) {
        super(url, message, asynchronous);
        setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    /**
     * {@inheritDoc}.
     * Prepends the string "{@code logoutRequest=}" to the message body.
     */
    @Override
    protected String formatOutputMessageInternal(final String message) {
        return LOGOUT_PARAMETER_NAME + '=' + super.formatOutputMessageInternal(message);
    }
}
