package org.apereo.cas.configuration.model.core.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * This is {@link AuthenticationExceptionsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.custom.authentication", ignoreUnknownFields = false)
public class AuthenticationExceptionsProperties {
    private List exceptions;

    public List getExceptions() {
        return exceptions;
    }

    public void setExceptions(final List exceptions) {
        this.exceptions = exceptions;
    }
}


