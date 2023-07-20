package org.apereo.cas.support.events.authentication.adaptive;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.AbstractCasEvent;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasRiskyAuthenticationMitigatedEvent}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@Getter
public class CasRiskyAuthenticationMitigatedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 291198069766263578L;

    private final Authentication authentication;

    private final RegisteredService service;

    private final Object response;

    public CasRiskyAuthenticationMitigatedEvent(final Object source, final Authentication authentication,
                                                final RegisteredService service, final Object response,
                                                final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.authentication = authentication;
        this.service = service;
        this.response = response;
    }
}
