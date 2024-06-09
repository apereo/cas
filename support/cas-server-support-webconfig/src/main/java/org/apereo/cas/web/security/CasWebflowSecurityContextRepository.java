package org.apereo.cas.web.security;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.web.flow.executor.CasFlowExecutor;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.TransientSecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.executor.FlowExecutor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link CasWebflowSecurityContextRepository}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class CasWebflowSecurityContextRepository implements SecurityContextRepository {
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final FlowExecutor loginFlowExecutor;
    private final FlowUrlHandler loginFlowUrlHandler;

    @Override
    public SecurityContext loadContext(final HttpRequestResponseHolder requestResponseHolder) {
        val request = requestResponseHolder.getRequest();
        if (containsContext(request)) {
            val authentication = Objects.requireNonNull(getInProgressAuthentication(request));
            val principal = authentication.getPrincipal();
            val authorities = principal.getAttributes().keySet().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            val springSecurityAuthentication = new PreAuthenticatedAuthenticationToken(principal, authentication.getCredentials(), authorities);
            springSecurityAuthentication.setAuthenticated(true);
            springSecurityAuthentication.setDetails(new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(request, authorities));
            return new TransientSecurityContext(springSecurityAuthentication);
        }
        return securityContextHolderStrategy.createEmptyContext();
    }

    @Override
    public void saveContext(final SecurityContext context, final HttpServletRequest request, final HttpServletResponse response) {
    }

    @Override
    public boolean containsContext(final HttpServletRequest request) {
        return getInProgressAuthentication(request) != null;
    }

    private Authentication getInProgressAuthentication(final HttpServletRequest request) {
        val flowExecutionRepository = ((CasFlowExecutor) loginFlowExecutor).getFlowExecutionRepository();
        val execution = loginFlowUrlHandler.getFlowExecutionKey(request);
        if (StringUtils.isNotBlank(execution)) {
            val flowExecutionKey = flowExecutionRepository.parseFlowExecutionKey(execution);
            val flowExecution = flowExecutionRepository.getFlowExecution(flowExecutionKey);
            return WebUtils.getAuthentication(flowExecution.getConversationScope());
        }
        return null;
    }
}
