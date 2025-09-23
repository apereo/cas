package org.apereo.cas.web.security;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.spring.SecurityContextUtils;
import org.apereo.cas.web.flow.executor.CasFlowExecutor;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
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
@Slf4j
public class CasWebflowSecurityContextRepository implements SecurityContextRepository {
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public SecurityContext loadContext(final HttpRequestResponseHolder requestResponseHolder) {
        val request = requestResponseHolder.getRequest();
        if (containsContext(request)) {
            val authentication = Objects.requireNonNull(getInProgressAuthentication(request));
            val principal = authentication.getPrincipal();
            return SecurityContextUtils.createSecurityContext(principal, request);
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
        val flowExecutors = applicationContext.getBeansOfType(FlowExecutor.class)
            .values()
            .stream()
            .filter(CasFlowExecutor.class::isInstance)
            .map(CasFlowExecutor.class::cast)
            .collect(Collectors.toSet());
        for (val casFlowExecutor : flowExecutors) {
            val flowExecutionRepository = casFlowExecutor.getFlowExecutionRepository();
            val execution = casFlowExecutor.getFlowUrlHandler().getFlowExecutionKey(request);
            if (StringUtils.isNotBlank(execution)) {
                try {
                    val flowExecutionKey = flowExecutionRepository.parseFlowExecutionKey(execution);
                    val flowExecution = flowExecutionRepository.getFlowExecution(flowExecutionKey);
                    return WebUtils.getAuthentication(flowExecution.getConversationScope());
                } catch (final Exception e) {
                    LOGGER.debug("Unable to determine authentication attempt from the webflow context: [{}]", e.getMessage());
                    LOGGER.trace(e.getMessage(), e);
                }
            }
        }
        return null;
    }
}
