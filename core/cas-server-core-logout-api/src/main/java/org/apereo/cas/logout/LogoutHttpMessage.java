package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.web.HttpMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;

/**
 * A logout http message that is accompanied by a special content type
 * and formatting.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Setter
@Getter
@SuperBuilder
public class LogoutHttpMessage extends HttpMessage {

    @Serial
    private static final long serialVersionUID = 399581521957873727L;

    private final @Nullable String logoutRequestParameter;

    public LogoutHttpMessage(@Nullable final String logoutRequestParameter, final URL url,
                             final String message, final boolean asynchronous) {
        super(url, message, asynchronous);
        setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        this.logoutRequestParameter = logoutRequestParameter;
    }

    public LogoutHttpMessage(final URL url, final String message, final boolean asynchronous) {
        this(null, url, message, asynchronous);
    }

    @Override
    protected String formatOutputMessageInternal(final String message) {
        var formatted = super.formatOutputMessageInternal(message);
        if (logoutRequestParameter != null) {
            formatted = String.format("%s=%s", this.logoutRequestParameter, formatted);
        }
        return formatted;
    }
}
