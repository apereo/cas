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
        if (returnValue instanceof final Response value) {
            return getPrincipalIdFromSamlResponse(value);
        }
        if (returnValue instanceof final Envelope value) {
            return getPrincipalIdFromSamlEcpResponse(value);
        }
        LOGGER.error("Could not determine the SAML response in the returned value");
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    protected String[] getPrincipalIdFromSamlEcpResponse(final Envelope envelope) {
        val objects = envelope.getBody().getUnknownXMLObjects();
        if (objects.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        val object = objects.getFirst();
        if (object instanceof final Response response) {
            return getPrincipalIdFromSamlResponse(response);
        }
        if (object instanceof final Fault fault) {
            return getPrincipalIdFromSamlEcpFault(fault);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    protected String[] getPrincipalIdFromSamlResponse(final Response response) {
        val values = new HashMap<>();
        values.put("issuer", response.getIssuer().getValue());
        values.put("destination", response.getDestination());
        values.put("responseId", response.getID());
        return new String[]{auditFormat.serialize(values)};
    }

    protected String[] getPrincipalIdFromSamlEcpFault(final Fault fault) {
        val values = new HashMap<>();
        values.put("actor", fault.getActor().getURI());
        values.put("message", fault.getMessage().getValue());
        return new String[]{auditFormat.serialize(values)};
    }
}
