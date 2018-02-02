package org.apereo.cas.authentication.audit;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.spi.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

/**
 * This is {@link SurrogateAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SurrogateAuditPrincipalIdProvider extends DefaultAuditPrincipalIdProvider {

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public String getPrincipalIdFrom(final Authentication authentication, final Object returnValue, final Exception exception) {
        if (authentication == null) {
            return Credential.UNKNOWN_ID;
        }
        if (supports(authentication, returnValue, exception)) {
            final String surrogateUser = authentication.getAttributes()
                .get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER).toString();
            final String principalId = authentication.getAttributes()
                .get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL).toString();
            return String.format("(Primary User: [%s], Surrogate User: [%s])", principalId, surrogateUser);
        }
        return authentication.getPrincipal().getId();
    }

    @Override
    public boolean supports(final Authentication authentication, final Object resultValue, final Exception exception) {
        return super.supports(authentication, resultValue, exception)
            && authentication.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER);
    }
}
