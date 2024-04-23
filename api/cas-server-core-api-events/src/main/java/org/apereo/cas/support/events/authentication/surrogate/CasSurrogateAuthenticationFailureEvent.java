package org.apereo.cas.support.events.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasSurrogateAuthenticationFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasSurrogateAuthenticationFailureEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 8059647975948452375L;

    private final Principal principal;

    private final String surrogate;

    public CasSurrogateAuthenticationFailureEvent(final Object source, final Principal principal, final String surrogate, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.principal = principal;
        this.surrogate = surrogate;
    }
}
