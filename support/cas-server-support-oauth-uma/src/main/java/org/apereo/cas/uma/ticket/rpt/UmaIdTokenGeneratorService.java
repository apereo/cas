package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseIdTokenGeneratorService;
import org.apereo.cas.ticket.IdTokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link UmaIdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaIdTokenGeneratorService extends BaseIdTokenGeneratorService {
    public UmaIdTokenGeneratorService(final CasConfigurationProperties casProperties,
                                      final IdTokenSigningAndEncryptionService signingService,
                                      final ServicesManager servicesManager,
                                      final TicketRegistry ticketRegistry) {
        super(casProperties, signingService, servicesManager, ticketRegistry);
    }

    @Override
    public String generate(final HttpServletRequest request, final HttpServletResponse response,
                           final AccessToken accessTokenId, final long timeoutInSeconds,
                           final OAuth20ResponseTypes responseType,
                           final OAuthRegisteredService registeredService) {
        return null;
    }
}
