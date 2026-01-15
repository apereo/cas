package org.apereo.cas.support.events.authentication.adaptive;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link CasRiskBasedAuthenticationMitigationStartedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRiskBasedAuthenticationMitigationStartedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 123568299766263298L;

    private final Authentication authentication;

    private final RegisteredService service;

    private final Object score;

    public CasRiskBasedAuthenticationMitigationStartedEvent(final Object source, final Authentication authentication,
                                                            final RegisteredService service, final Object score,
                                                            final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.authentication = authentication;
        this.service = service;
        this.score = score;
    }
}
