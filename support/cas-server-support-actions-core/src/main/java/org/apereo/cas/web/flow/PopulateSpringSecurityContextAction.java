package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * This is {@link PopulateSpringSecurityContextAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class PopulateSpringSecurityContextAction extends BaseCasWebflowAction {
    private final ObjectProvider<SecurityContextRepository> securityContextRepository;

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
        val authn = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authn.getPrincipal(), requestContext);
        val authorities = principal.getAttributes().keySet().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        val user = new User(principal.getId(), RandomUtils.generateSecureRandomId(), authorities);
        val secAuth = new PreAuthenticatedAuthenticationToken(user, authn.getCredentials(), authorities);
        secAuth.setAuthenticated(true);

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        secAuth.setDetails(new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(request, authorities));
        val context = SecurityContextHolder.getContext();
        context.setAuthentication(secAuth);
        val session = request.getSession(true);
        LOGGER.trace("Storing security context in session [{}] for [{}]", session.getId(), principal);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return context;
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
