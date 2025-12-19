package org.apereo.cas.support.events.authentication;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;


/**
 * This is {@link CasAuthenticationTransactionStartedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
public class CasAuthenticationTransactionStartedEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = -1862937393590213811L;

    private final Credential credential;

    public CasAuthenticationTransactionStartedEvent(final Object source, final Credential c, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.credential = c;
    }
}
