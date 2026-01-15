package org.apereo.cas.support.oauth.web.audit;

import module java.base;
import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;

/**
 * The {@link OAuth20AccessTokenResponseAuditResourceResolver} for audit advice.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20AccessTokenResponseAuditResourceResolver extends OAuth20AuthorizationResponseAuditResourceResolver {
    public OAuth20AccessTokenResponseAuditResourceResolver(final AuditEngineProperties properties) {
        super(properties);
    }
}
