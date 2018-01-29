package org.apereo.cas.support.saml.web.idp.audit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;

import java.util.List;

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
        if (returnValue instanceof Response) {
            return getPrincipalIdFromSamlResponse((Response) returnValue);
        }
        if (returnValue instanceof Envelope) {
            return getPrincipalIdFromSamlEcpResponse((Envelope) returnValue);
        }
        LOGGER.error("Could not determine the SAML response in the returned value");
        return new String[]{};
    }

    private String[] getPrincipalIdFromSamlEcpResponse(final Envelope envelope) {
        final List<XMLObject> objects = envelope.getBody().getUnknownXMLObjects();
        if (objects.isEmpty()) {
            return new String[]{};
        }
        final XMLObject object = objects.get(0);
        if (object instanceof Response) {
            return getPrincipalIdFromSamlResponse((Response) object);
        }
        if (object instanceof Fault) {
            return getPrincipalIdFromSamlEcpFault((Fault) object);
        }
        return new String[]{};
    }

    private String[] getPrincipalIdFromSamlResponse(final Response response) {
        final String result =
            new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("issuer", response.getIssuer().getValue())
                .append("destination", response.getDestination())
                .toString();
        return new String[]{result};
    }

    private String[] getPrincipalIdFromSamlEcpFault(final Fault fault) {
        final String result =
            new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("actor", fault.getActor().getValue())
                .append("message", fault.getMessage().getValue())
                .toString();
        return new String[]{result};
    }
}
