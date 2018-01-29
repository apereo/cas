package org.apereo.cas.support.saml.web.idp.audit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;

/**
 * This is {@link SamlRequestAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class SamlRequestAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint joinPoint, final Object returnValue) {
        if (returnValue instanceof Pair) {
            return getAuditResourceFromSamlRequest((Pair) returnValue);
        }
        return new String[]{};
    }

    private String[] getAuditResourceFromSamlRequest(final Pair result) {
        final XMLObject returnValue = (XMLObject) result.getLeft();
        if (returnValue instanceof AuthnRequest) {
            return getAuditResourceFromSamlAuthnRequest((AuthnRequest) returnValue);
        }
        if (returnValue instanceof LogoutRequest) {
            return getAuditResourceFromSamlLogoutRequest((LogoutRequest) returnValue);
        }
        return new String[]{};
    }

    private String[] getAuditResourceFromSamlLogoutRequest(final LogoutRequest returnValue) {
        final LogoutRequest request = returnValue;
        final String result =
            new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("issuer", request.getIssuer().getValue())
                .toString();
        return new String[]{result};
    }

    private String[] getAuditResourceFromSamlAuthnRequest(final AuthnRequest returnValue) {
        final AuthnRequest request = returnValue;
        final String result =
            new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("issuer", request.getIssuer().getValue())
                .append("binding", request.getProtocolBinding())
                .toString();
        return new String[]{result};
    }
}
