package org.apereo.cas.support.events.service;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * This is {@link CasRegisteredServicesRefreshEvent} that is signaled
 * when a registered service is saved into the CAS registry.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@ToString(callSuper = true)
@Getter
public class CasRegisteredServicesRefreshEvent extends BaseCasRegisteredServiceEvent {

    @Serial
    private static final long serialVersionUID = 291168299766263298L;

    /**
     * Instantiates a new cas sso event.
     *
     * @param source the source
     */
    public CasRegisteredServicesRefreshEvent(final Object source, final ClientInfo clientInfo) {
        super(source, clientInfo);
    }
}
