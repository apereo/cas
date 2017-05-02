package org.apereo.cas.support.events.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;

/**
 * This is {@link CasAuthenticationPrincipalResolvedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasAuthenticationPrincipalResolvedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -1862937393594313844L;
    
    private final Principal principal;


    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source the source
     * @param p      the principal
     */
    public CasAuthenticationPrincipalResolvedEvent(final Object source, final Principal p) {
        super(source);
        this.principal = p;
    }

    public Principal getPrincipal() {
        return principal;
    }
}
