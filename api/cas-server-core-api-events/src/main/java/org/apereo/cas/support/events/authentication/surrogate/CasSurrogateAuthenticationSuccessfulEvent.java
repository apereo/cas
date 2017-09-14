package org.apereo.cas.support.events.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasSurrogateAuthenticationSuccessfulEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasSurrogateAuthenticationSuccessfulEvent extends AbstractCasEvent {
    private static final long serialVersionUID = 8059647975948452375L;

    private final Principal principal;
    private final String surrogate;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source    the source
     * @param principal the principal
     * @param surrogate the surrogate
     */
    public CasSurrogateAuthenticationSuccessfulEvent(final Object source, final Principal principal, final String surrogate) {
        super(source);
        this.principal = principal;
        this.surrogate = surrogate;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public String getSurrogate() {
        return surrogate;
    }
}
