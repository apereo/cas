package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.audit.spi.principal.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;

import lombok.val;
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
        val response = (Response) returnValue;
        if (!response.getAssertions().isEmpty()) {
            val assertion = response.getAssertions().get(0);
            val subject = assertion.getSubject();
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
