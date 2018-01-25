package org.apereo.cas.support.saml;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;

/**
 * This is {@link SamlResponseAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SamlResponseAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        final Response response = (Response) returnValue;
        if (response != null) {
            final String result =
                new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                    .append("issuer", response.getIssuer().getValue())
                    .append("destination", response.getDestination())
                    .toString();
            return new String[]{result};
        }
        LOGGER.error("Could not determine the SAML response in the returned value");
        return new String[]{};
    }
}
