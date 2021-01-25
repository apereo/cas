package org.apereo.cas.support.oauth.web.audit;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.HashMap;

/**
 * The {@link OAuth20AccessTokenGrantRequestAuditResourceResolver} for audit advice
 * weaved at {@link org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor#extract} join point.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class OAuth20AccessTokenGrantRequestAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        val auditResult = (AuditableExecutionResult) retval;
        val executionResult = auditResult.getExecutionResult();

        if (executionResult.isPresent()) {
            val accessTokenRequest = (AccessTokenRequestDataHolder) executionResult.get();
            val tokenId = accessTokenRequest.getToken() == null ? "N/A" : accessTokenRequest.getToken().getId();
            val values = new HashMap<>();
            values.put("token", tokenId);
            values.put("client_id", accessTokenRequest.getRegisteredService().getClientId());
            values.put("service", accessTokenRequest.getService().getId());
            values.put("grant_type", accessTokenRequest.getGrantType().getType());
            values.put("response_type", accessTokenRequest.getResponseType().getType());
            values.put("scopes", accessTokenRequest.getScopes());
            return new String[]{auditFormat.serialize(values)};
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
