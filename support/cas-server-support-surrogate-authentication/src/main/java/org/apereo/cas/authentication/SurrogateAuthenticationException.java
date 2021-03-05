package org.apereo.cas.authentication;

/**
 * This is {@link SurrogateAuthenticationException}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogateAuthenticationException extends AuthenticationException {
    private static final long serialVersionUID = -3250559691638860076L;

    public SurrogateAuthenticationException(final String msg) {
        super(msg);
    }
}
