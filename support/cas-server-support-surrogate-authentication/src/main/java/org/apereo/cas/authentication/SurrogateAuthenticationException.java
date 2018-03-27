package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This is {@link SurrogateAuthenticationException}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SurrogateAuthenticationException extends AuthenticationException {
    private static final long serialVersionUID = -3250559691638860076L;

    public SurrogateAuthenticationException(final String msg) {
        super(msg);
    }

    public SurrogateAuthenticationException(final Map<String, Throwable> handlerErrors) {
        super(handlerErrors);
    }
}
