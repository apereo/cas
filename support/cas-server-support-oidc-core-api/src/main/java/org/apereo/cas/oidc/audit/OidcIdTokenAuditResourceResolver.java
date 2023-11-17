package org.apereo.cas.oidc.audit;

import org.apereo.cas.configuration.model.core.audit.AuditEngineProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.OidcIdToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.pac4j.core.profile.UserProfile;
import java.util.HashMap;

/**
 * This is {@link OidcIdTokenAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class OidcIdTokenAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    private final AuditEngineProperties properties;

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        val values = new HashMap<>();
        val idToken = (OidcIdToken) returnValue;
        values.put(OidcConstants.ID_TOKEN, DigestUtils.abbreviate(idToken.token(), properties.getAbbreviationLength()));
        val accessToken = (OAuth20AccessToken) auditableTarget.getArgs()[0];
        val userProfile = (UserProfile) auditableTarget.getArgs()[1];
        values.put(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        values.put(OAuth20Constants.SCOPE, accessToken.getScopes());
        FunctionUtils.doIfNotNull(userProfile, __ -> values.put("username", userProfile.getId()));
        FunctionUtils.doIfNotNull(accessToken.getService(), svc -> values.put("service", svc));
        if (idToken.claims().hasClaim(OidcConstants.TXN)) {
            val txn = FunctionUtils.doUnchecked(() -> idToken.claims().getStringClaimValue(OidcConstants.TXN));
            FunctionUtils.doIfNotNull(txn, __ -> values.put(OidcConstants.TXN, txn));
            values.put("authn_methods", accessToken.getAuthentication().getSuccesses().keySet());
        }
        return new String[]{auditFormat.serialize(values)};
    }
}

