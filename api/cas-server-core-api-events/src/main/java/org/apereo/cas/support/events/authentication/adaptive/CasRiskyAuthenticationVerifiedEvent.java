package org.apereo.cas.support.events.authentication.adaptive;

import module java.base;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRiskyAuthenticationVerifiedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRiskyAuthenticationVerifiedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 291168297497263298L;

    private final String riskToken;
    
    public CasRiskyAuthenticationVerifiedEvent(final Object source,
                                               final ClientInfo clientInfo,
                                               final String riskToken) {
        super(source, clientInfo);
        this.riskToken = riskToken;
    }
}
