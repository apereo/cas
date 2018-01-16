package org.apereo.cas.configuration.model.core.ticket;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ProxyGrantingTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Slf4j
@Getter
@Setter
public class ProxyGrantingTicketProperties implements Serializable {

    private static final long serialVersionUID = 8478961497316814687L;

    /**
     * Maximum length of the proxy granting ticket, when generating one.
     */
    private int maxLength = 50;
}
