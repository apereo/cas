package org.apereo.cas.support.events.authentication;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;


/**
 * This is {@link CasAuthenticationTransactionSuccessfulEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString(callSuper = true)
@Getter
public class CasAuthenticationTransactionSuccessfulEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = 8059647975948452375L;

    private final Credential credential;

    public CasAuthenticationTransactionSuccessfulEvent(final Object source, final Credential c, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.credential = c;
    }
}
