package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collection;

/**
 * This is {@link AccessTokenGrantAuditableRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class AccessTokenGrantAuditableRequestExtractor extends BaseAuditableExecution {
    private final Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    @Audit(action = "OAUTH2_ACCESS_TOKEN_REQUEST",
        actionResolverName = "OAUTH2_ACCESS_TOKEN_REQUEST_ACTION_RESOLVER",
        resourceResolverName = "OAUTH2_ACCESS_TOKEN_REQUEST_RESOURCE_RESOLVER")
    @Override
    public AuditableExecutionResult execute(final AuditableContext context) {
        val request = (HttpServletRequest) context.getRequest().orElseThrow();
        val response = (HttpServletResponse) context.getResponse().orElseThrow();

        val result = this.accessTokenGrantRequestExtractors.stream()
            .filter(ext -> ext.supports(request))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("Access token request is not supported"))
            .extract(request, response);

        return AuditableExecutionResult.builder().executionResult(result).build();
    }
}
