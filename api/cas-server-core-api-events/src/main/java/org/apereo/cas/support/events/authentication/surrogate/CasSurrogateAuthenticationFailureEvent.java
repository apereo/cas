package org.apereo.cas.support.events.authentication.surrogate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasSurrogateAuthenticationFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasSurrogateAuthenticationFailureEvent extends AbstractCasEvent {
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
    public CasSurrogateAuthenticationFailureEvent(final Object source, final Principal principal, final String surrogate) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("principal", principal)
                .append("surrogate", surrogate)
                .toString();
    }
}
