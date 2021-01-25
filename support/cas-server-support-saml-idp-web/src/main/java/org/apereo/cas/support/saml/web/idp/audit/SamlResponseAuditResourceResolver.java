package org.apereo.cas.support.saml.web.idp.audit;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;

import java.util.HashMap;

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
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private String[] getPrincipalIdFromSamlEcpResponse(final Envelope envelope) {
        val objects = envelope.getBody().getUnknownXMLObjects();
        if (objects.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        val object = objects.get(0);
        if (object instanceof Response) {
            return getPrincipalIdFromSamlResponse((Response) object);
        }
        if (object instanceof Fault) {
            return getPrincipalIdFromSamlEcpFault((Fault) object);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private String[] getPrincipalIdFromSamlResponse(final Response response) {
        val values = new HashMap<>();
        values.put("issuer", response.getIssuer().getValue());
        values.put("destination", response.getDestination());
        return new String[]{auditFormat.serialize(values)};
    }

    private String[] getPrincipalIdFromSamlEcpFault(final Fault fault) {
        val values = new HashMap<>();
        values.put("actor", fault.getActor().getURI());
        values.put("message", fault.getMessage().getValue());
        return new String[]{auditFormat.serialize(values)};
    }
}
