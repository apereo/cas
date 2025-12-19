package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.audit.BaseAuditableExecution;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.jee.context.JEEContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenGrantAuditableRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class AccessTokenGrantAuditableRequestExtractor extends BaseAuditableExecution {
    private final Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    @Audit(action = AuditableActions.OAUTH2_ACCESS_TOKEN_REQUEST,
        actionResolverName = AuditActionResolvers.OAUTH2_ACCESS_TOKEN_REQUEST_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_ACCESS_TOKEN_REQUEST_RESOURCE_RESOLVER)
    @Override
    public AuditableExecutionResult execute(final AuditableContext auditableContext) throws Throwable {
        val request = (HttpServletRequest) auditableContext.getRequest().orElseThrow();
        val response = (HttpServletResponse) auditableContext.getResponse().orElseThrow();

        val context = new JEEContext(request, response);
        val tokenRequestContext = accessTokenGrantRequestExtractors
            .stream()
            .filter(ext -> ext.supports(context))
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("Access token request is not supported"))
            .extract(context);

        return AuditableExecutionResult
            .builder()
            .authentication(tokenRequestContext.getAuthentication())
            .service(tokenRequestContext.getService())
            .registeredService(tokenRequestContext.getRegisteredService())
            .executionResult(tokenRequestContext)
            .build();
    }
}
