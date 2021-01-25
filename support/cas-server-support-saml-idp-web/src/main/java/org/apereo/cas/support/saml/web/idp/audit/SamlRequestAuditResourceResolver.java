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
        if (returnValue instanceof Pair) {
            return getAuditResourceFromSamlRequest((Pair) returnValue);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private String[] getAuditResourceFromSamlRequest(final Pair result) {
        val returnValue = (XMLObject) result.getLeft();
        if (returnValue instanceof AuthnRequest) {
            return getAuditResourceFromSamlAuthnRequest((AuthnRequest) returnValue);
        }
        if (returnValue instanceof LogoutRequest) {
            return getAuditResourceFromSamlLogoutRequest((LogoutRequest) returnValue);
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private String[] getAuditResourceFromSamlLogoutRequest(final LogoutRequest returnValue) {
        val values = new HashMap<>();
        values.put("issuer", returnValue.getIssuer().getValue());
        return new String[]{auditFormat.serialize(values)};
    }

    private String[] getAuditResourceFromSamlAuthnRequest(final AuthnRequest returnValue) {
        val values = new HashMap<>();
        values.put("issuer", returnValue.getIssuer().getValue());
        values.put("binding", returnValue.getProtocolBinding());
        return new String[]{auditFormat.serialize(values)};
    }
}
