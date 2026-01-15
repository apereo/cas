package org.apereo.cas.support.events.authentication;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;


/**
 * This is {@link CasAuthenticationTransactionCompletedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
public class CasAuthenticationTransactionCompletedEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = -1862538693590213844L;

    private final Authentication authentication;

    public CasAuthenticationTransactionCompletedEvent(final Object source, final Authentication authentication, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.authentication = authentication;
    }
}
