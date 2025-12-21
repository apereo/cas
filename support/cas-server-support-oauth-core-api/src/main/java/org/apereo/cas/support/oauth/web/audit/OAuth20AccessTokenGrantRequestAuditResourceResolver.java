package org.apereo.cas.support.oauth.web.audit;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * The {@link OAuth20AccessTokenGrantRequestAuditResourceResolver} for audit advice
 * weaved at {@link org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor#extract} join point.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class OAuth20AccessTokenGrantRequestAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, @Nullable final Object retval) {
        val auditResult = Objects.requireNonNull((AuditableExecutionResult) retval);
        val executionResult = auditResult.getExecutionResult();

        if (executionResult.isPresent()) {
            val accessTokenRequest = (AccessTokenRequestContext) executionResult.get();
            val tokenId = accessTokenRequest.getToken() == null ? "N/A" : accessTokenRequest.getToken().getId();
            val values = new HashMap<>();
            values.put(CasProtocolConstants.PARAMETER_SERVICE, accessTokenRequest.getService().getId());
            values.put(OAuth20Constants.CODE, tokenId);
            values.put(OAuth20Constants.CLIENT_ID, accessTokenRequest.getClientId());
            values.put(OAuth20Constants.GRANT_TYPE, accessTokenRequest.getGrantType().getType());
            values.put(OAuth20Constants.RESPONSE_TYPE, accessTokenRequest.getResponseType().getType());
            values.put(OAuth20Constants.REDIRECT_URI, accessTokenRequest.getRedirectUri());
            values.put(OAuth20Constants.SCOPE, accessTokenRequest.getScopes());
            return new String[]{auditFormat.serialize(values)};
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
