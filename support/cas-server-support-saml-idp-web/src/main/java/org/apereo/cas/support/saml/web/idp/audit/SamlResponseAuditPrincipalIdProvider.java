package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.audit.spi.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.opensaml.saml.saml2.core.Response;

/**
 * This is {@link SamlResponseAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SamlResponseAuditPrincipalIdProvider extends DefaultAuditPrincipalIdProvider {
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public String getPrincipalIdFrom(final Authentication authentication, final Object returnValue, final Exception exception) {
        final var response = (Response) returnValue;
        if (response.getAssertions().size() > 0) {
            final var assertion = response.getAssertions().get(0);
            final var subject = assertion.getSubject();
            if (subject != null && subject.getNameID() != null) {
                return subject.getNameID().getValue();
            }
        }
        return super.getPrincipalIdFrom(authentication, returnValue, exception);
    }

    @Override
    public boolean supports(final Authentication authentication, final Object resultValue, final Exception exception) {
        return resultValue != null && resultValue instanceof Response;
    }
}
