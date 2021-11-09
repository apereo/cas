package org.apereo.cas.uma;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link UmaConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class UmaConfigurationContext extends OAuth20ConfigurationContext {
    private final UmaPermissionTicketFactory umaPermissionTicketFactory;

    private final ResourceSetRepository umaResourceSetRepository;

    private final CasConfigurationProperties casProperties;

    private final UmaResourceSetClaimPermissionExaminer claimPermissionExaminer;

    private final IdTokenGeneratorService requestingPartyTokenGenerator;
}
