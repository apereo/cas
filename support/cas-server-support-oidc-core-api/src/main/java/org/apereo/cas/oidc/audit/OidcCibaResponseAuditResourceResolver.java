package org.apereo.cas.oidc.audit;

import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.ciba.OidcCibaResponse;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.DigestUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
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
        val responseEntity = (ResponseEntity) returnValue;
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            val cibaResponse = Objects.requireNonNull((OidcCibaResponse) responseEntity.getBody());
            values.put(OidcConstants.AUTH_REQ_ID, DigestUtils.abbreviate(cibaResponse.getAuthenticationRequestId(), properties.getAbbreviationLength()));
        } else if (responseEntity.hasBody()) {
            val body = (Map) responseEntity.getBody();
            values.put("status", responseEntity.getStatusCode().toString());
            values.put(OAuth20Constants.ERROR_DESCRIPTION, body.get(OAuth20Constants.ERROR_DESCRIPTION));
        }
        return new String[]{auditFormat.serialize(values)};
    }
}
