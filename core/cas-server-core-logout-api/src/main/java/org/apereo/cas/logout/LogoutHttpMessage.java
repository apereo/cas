package org.apereo.cas.logout;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.http.HttpMessage;
import org.springframework.http.MediaType;
import java.net.URL;
import lombok.Setter;

/**
 * A logout http message that is accompanied by a special content type
 * and formatting.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Setter
public class LogoutHttpMessage extends HttpMessage {

    private static final long serialVersionUID = 399581521957873727L;

    /** The parameter name that contains the logout request. */
    private static final String LOGOUT_REQUEST_PARAMETER = "logoutRequest";

    private boolean prefixLogoutParameterName = true;

    /**
     * Constructs a logout message.
     *
     * @param url          The url to send the message to
     * @param message      Message to send to the url
     * @param asynchronous the asynchronous
     */
    public LogoutHttpMessage(final URL url, final String message, final boolean asynchronous) {
        super(url, message, asynchronous);
        setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    /**
     * {@inheritDoc}.
     * Prepends the string "{@code logoutRequest=}" to the message body.
     */
    @Override
    protected String formatOutputMessageInternal(final String message) {
        return (this.prefixLogoutParameterName ? LOGOUT_REQUEST_PARAMETER + '=' : StringUtils.EMPTY) + super.formatOutputMessageInternal(message);
    }
}
