package org.apereo.cas.support.oauth.web.audit;

import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

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

            val result = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
                .append("token", tokenId)
                .append("client_id", accessTokenRequest.getRegisteredService().getClientId())
                .append("service", accessTokenRequest.getService().getId())
                .append("grant_type", accessTokenRequest.getGrantType().getType())
                .append("response_type", accessTokenRequest.getResponseType().getType())
                .append("scopes", accessTokenRequest.getScopes())
                .toString();
            return new String[]{result};
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
