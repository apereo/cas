package org.apereo.cas.uma;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;

/**
 * This is {@link UmaConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class UmaConfigurationContext {
    private final OAuth20TokenSigningAndEncryptionService signingService;
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final JwtBuilder accessTokenJwtBuilder;
    private final UmaPermissionTicketFactory umaPermissionTicketFactory;
    private final ResourceSetRepository umaResourceSetRepository;
    private final CasConfigurationProperties casProperties;
    private final UmaResourceSetClaimPermissionExaminer claimPermissionExaminer;
    private final IdTokenGeneratorService requestingPartyTokenGenerator;
    private final SessionStore<JEEContext> sessionStore;
}
