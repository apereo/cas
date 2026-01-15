package org.apereo.cas.support.events.authentication.surrogate;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasSurrogateAuthenticationSuccessfulEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@ToString(callSuper = true)
public class CasSurrogateAuthenticationSuccessfulEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 8059647975948452375L;

    private final Principal principal;

    private final String surrogate;

    public CasSurrogateAuthenticationSuccessfulEvent(final Object source, final Principal principal, final String surrogate, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.principal = principal;
        this.surrogate = surrogate;
    }
}
