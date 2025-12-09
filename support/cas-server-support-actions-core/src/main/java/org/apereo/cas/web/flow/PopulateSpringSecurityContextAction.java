package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.SecurityContextUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.ArrayList;

/**
 * This is {@link PopulateSpringSecurityContextAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class PopulateSpringSecurityContextAction extends BaseCasWebflowAction {
    private final ObjectProvider<@NonNull SecurityContextRepository> securityContextRepository;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = buildAuthenticationContext(requestContext);
        securityContextRepository.ifAvailable(secContext -> secContext.saveContext(context, request, response));
        SecurityContextHolder.setContext(context);
        return null;
    }

    protected SecurityContext buildAuthenticationContext(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        return SecurityContextUtils.createSecurityContext(principal, request);
    }

    protected Principal resolvePrincipal(final Principal principal, final RequestContext requestContext) {
        val resolvers = new ArrayList<>(requestContext.getActiveFlow().getApplicationContext()
            .getBeansOfType(MultifactorAuthenticationPrincipalResolver.class).values());
        AnnotationAwareOrderComparator.sort(resolvers);
        return resolvers
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElse(principal);
    }
}
