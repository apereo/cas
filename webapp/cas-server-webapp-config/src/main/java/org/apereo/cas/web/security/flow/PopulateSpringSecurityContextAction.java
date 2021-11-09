package org.apereo.cas.web.security.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.stream.Collectors;

/**
 * This is {@link PopulateSpringSecurityContextAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class PopulateSpringSecurityContextAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authn = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authn.getPrincipal());
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val authorities = principal.getAttributes().keySet().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        val secAuth = new PreAuthenticatedAuthenticationToken(principal, authn.getCredentials(), authorities);
        secAuth.setAuthenticated(true);
        secAuth.setDetails(new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(request, authorities));
        val context = SecurityContextHolder.getContext();
        context.setAuthentication(secAuth);
        val session = request.getSession(true);
        LOGGER.trace("Storing security context in session [{}] for [{}]", session.getId(), principal);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return null;
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
