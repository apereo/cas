package org.apereo.cas.support.saml.web.idp.audit;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;

import java.util.HashMap;

/**
 * This is {@link SamlRequestAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SamlRequestAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        if (returnValue instanceof final Pair context) {
            return getAuditResourceFromSamlRequest((XMLObject) context.getLeft());
        }
        if (returnValue instanceof final XMLObject xmlObject) {
            return getAuditResourceFromSamlRequest(xmlObject);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    protected String[] getAuditResourceFromSamlRequest(final XMLObject returnValue) {
        if (returnValue instanceof final AuthnRequest authnRequest) {
            return getAuditResourceFromSamlAuthnRequest(authnRequest);
        }
        if (returnValue instanceof final LogoutRequest logoutRequest) {
            return getAuditResourceFromSamlLogoutRequest(logoutRequest);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    protected String[] getAuditResourceFromSamlLogoutRequest(final LogoutRequest returnValue) {
        val values = new HashMap<>();
        values.put("issuer", returnValue.getIssuer().getValue());
        return new String[]{auditFormat.serialize(values)};
    }

    protected String[] getAuditResourceFromSamlAuthnRequest(final AuthnRequest returnValue) {
        val values = new HashMap<>();
        values.put("issuer", returnValue.getIssuer().getValue());
        values.put("binding", returnValue.getProtocolBinding());
        values.put("destination", returnValue.getDestination());
        return new String[]{auditFormat.serialize(values)};
    }
}
