package org.apereo.cas.support.oauth.web.audit;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * The {@link OAuth20UserProfileDataAuditResourceResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class OAuth20UserProfileDataAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, @Nullable final Object retval) {
        Objects.requireNonNull(retval, "User profile data must not be null");
        val profileMap = (Map) retval;
        val accessToken = (OAuth20AccessToken) auditableTarget.getArgs()[0];

        var service = profileMap.get(CasProtocolConstants.PARAMETER_SERVICE);
        if (service == null) {
            service = accessToken.getService();
        }
        var clientId = profileMap.get(OAuth20Constants.CLIENT_ID);
        if (clientId == null) {
            clientId = accessToken.getClientId();
        }

        val values = new HashMap<>();
        values.put("id", profileMap.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID));
        values.put(OAuth20Constants.CLIENT_ID, clientId);
        values.put("service", service);
        values.put(OAuth20Constants.SCOPE, accessToken.getScopes());
        values.put("attributes", profileMap.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
        return new String[]{auditFormat.serialize(values)};
    }
}
