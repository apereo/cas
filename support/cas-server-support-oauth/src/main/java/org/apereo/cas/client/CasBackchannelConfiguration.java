package org.apereo.cas.client;

import org.apereo.cas.CentralAuthenticationService;
import org.jasig.cas.client.validation.TicketValidator;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.WebContext;

/**
 * This is a configuration for pac4j CAS client that uses {@link CasBackchannelTicketValidator}.
 *
 * @author Kirill Gagarski
 * @since 6.1.0
 */
public class CasBackchannelConfiguration extends CasConfiguration {

    private final CentralAuthenticationService cas;

    public CasBackchannelConfiguration(final String loginUrl, final CentralAuthenticationService cas) {
        super(loginUrl);
        this.cas = cas;
    }

    @Override
    public TicketValidator retrieveTicketValidator(final WebContext context) {
        return new CasBackchannelTicketValidator(cas);
    }
}
