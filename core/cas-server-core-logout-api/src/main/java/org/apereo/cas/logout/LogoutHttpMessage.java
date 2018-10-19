package org.apereo.cas.logout;

import org.apereo.cas.util.http.HttpMessage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;

import java.net.URL;

/**
 * A logout http message that is accompanied by a special content type
 * and formatting.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Setter
@Getter
public class LogoutHttpMessage extends HttpMessage {

    /**
     * The parameter name that contains the logout request.
     */
    public static final String LOGOUT_REQUEST_PARAMETER = "logoutRequest";

    private static final long serialVersionUID = 399581521957873727L;

    public LogoutHttpMessage(final URL url, final String message, final boolean asynchronous) {
        super(url, message, asynchronous);
        setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    @Override
    protected String formatOutputMessageInternal(final String message) {
        return LOGOUT_REQUEST_PARAMETER + '=' + super.formatOutputMessageInternal(message);
    }
}
