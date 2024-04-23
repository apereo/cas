package org.apereo.cas.uma;

import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
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
    /**
     * Default bean name.
     */
    public static final String BEAN_NAME = "umaConfigurationContext";

    private final ResourceSetRepository umaResourceSetRepository;

    private final UmaResourceSetClaimPermissionExaminer claimPermissionExaminer;

    private final IdTokenGeneratorService requestingPartyTokenGenerator;
}
