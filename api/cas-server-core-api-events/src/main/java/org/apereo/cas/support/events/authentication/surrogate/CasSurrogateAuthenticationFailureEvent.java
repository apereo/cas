package org.apereo.cas.support.events.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;

/**
 * This is {@link CasSurrogateAuthenticationFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasSurrogateAuthenticationFailureEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 8059647975948452375L;

    private final Principal principal;

    private final String surrogate;

    public CasSurrogateAuthenticationFailureEvent(final Object source, final Principal principal, final String surrogate) {
        super(source);
        this.principal = principal;
        this.surrogate = surrogate;
    }
}
