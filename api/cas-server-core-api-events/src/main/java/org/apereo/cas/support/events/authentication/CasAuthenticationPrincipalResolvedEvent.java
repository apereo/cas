package org.apereo.cas.support.events.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasAuthenticationPrincipalResolvedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
public class CasAuthenticationPrincipalResolvedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -1862937393594313844L;

    private final Principal principal;

    public CasAuthenticationPrincipalResolvedEvent(final Object source, final Principal p, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.principal = p;
    }
}
