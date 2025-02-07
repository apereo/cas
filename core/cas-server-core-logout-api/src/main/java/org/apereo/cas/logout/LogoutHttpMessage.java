package org.apereo.cas.logout;

import org.apereo.cas.web.HttpMessage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 399581521957873727L;

    private final String logoutRequestParameter;

    public LogoutHttpMessage(final String logoutRequestParameter, final URL url, final String message, final boolean asynchronous) {
        super(url, message, asynchronous);
        setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        this.logoutRequestParameter = logoutRequestParameter;
    }

    @Override
    protected String formatOutputMessageInternal(final String message) {
        return this.logoutRequestParameter + '=' + super.formatOutputMessageInternal(message);
    }
}
