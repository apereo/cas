package org.apereo.cas.configuration.model.core.authentication;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AuthenticationExceptionsProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class AuthenticationExceptionsProperties {
    private List exceptions = new ArrayList<>();

    public List getExceptions() {
        return exceptions;
    }

    public void setExceptions(final List exceptions) {
        this.exceptions = exceptions;
    }
}


