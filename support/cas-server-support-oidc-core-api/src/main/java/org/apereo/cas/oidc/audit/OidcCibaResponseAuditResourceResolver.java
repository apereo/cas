package org.apereo.cas.oidc.audit;

import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.OidcCibaResponse;
import org.apereo.cas.util.DigestUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Objects;

/**
 * This is {@link OidcCibaResponseAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OidcCibaResponseAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    private final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        val values = new HashMap<>();
        val cibaResponse = Objects.requireNonNull((OidcCibaResponse) ((ResponseEntity) returnValue).getBody());
        values.put(OidcConstants.AUTH_REQ_ID, DigestUtils.abbreviate(cibaResponse.getAuthenticationRequestId(), properties.getAbbreviationLength()));
        return new String[]{auditFormat.serialize(values)};
    }
}
