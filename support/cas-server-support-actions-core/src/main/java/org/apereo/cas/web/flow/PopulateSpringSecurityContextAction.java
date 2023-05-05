package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;

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
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val context = buildAuthenticationContext(requestContext, request);
        securityContextRepository.ifAvailable(secContext -> secContext.saveContext(context, request, response));
        SecurityContextHolder.setContext(context);
        return null;
    }

    protected SecurityContext buildAuthenticationContext(final RequestContext requestContext, final HttpServletRequest request) {
        val authn = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authn.getPrincipal());
        val authorities = principal.getAttributes().keySet().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        val secAuth = new PreAuthenticatedAuthenticationToken(principal, authn.getCredentials(), authorities);
        secAuth.setAuthenticated(true);
        secAuth.setDetails(new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(request, authorities));
        val context = SecurityContextHolder.getContext();
        context.setAuthentication(secAuth);
        val session = request.getSession(true);
        LOGGER.trace("Storing security context in session [{}] for [{}]", session.getId(), principal);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return context;
    }

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    protected Principal resolvePrincipal(final Principal principal) {
        val resolvers = ApplicationContextProvider.getMultifactorAuthenticationPrincipalResolvers();
        return resolvers
            .stream()
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElse(principal);
    }
}
