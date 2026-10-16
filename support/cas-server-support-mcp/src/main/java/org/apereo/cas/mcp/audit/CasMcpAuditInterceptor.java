package org.apereo.cas.mcp.audit;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This is {@link CasMcpAuditInterceptor}. It wraps every MCP tool and resource method, outside method security so that
 * denied calls are recorded too, and hands each call to the {@link CasMcpAuditor}: who called, which operation, with which
 * arguments, and whether it succeeded, was denied or failed. A tool that calls other tools, such as explainLogin, is
 * recorded once: calls made while an audited call is running on the same thread are not recorded again.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasMcpAuditInterceptor implements MethodInterceptor {
    private static final int MAX_ARGUMENT_LENGTH = 256;

    private static final ScopedValue<Boolean> AUDITED_CALL = ScopedValue.newInstance();

    private final ObjectProvider<CasMcpAuditor> auditor;

    @Override
    public @Nullable Object invoke(final MethodInvocation invocation) throws Throwable {
        if (AUDITED_CALL.isBound()) {
            return invocation.proceed();
        }
        return ScopedValue.where(AUDITED_CALL, Boolean.TRUE).call(() -> audit(invocation));
    }

    private @Nullable Object audit(final MethodInvocation invocation) throws Throwable {
        try {
            val result = invocation.proceed();
            record(invocation, CasMcpOperation.Outcome.SUCCESS);
            return result;
        } catch (final AccessDeniedException | AuthenticationException e) {
            record(invocation, CasMcpOperation.Outcome.DENIED);
            throw e;
        } catch (final Throwable e) {
            record(invocation, CasMcpOperation.Outcome.FAILED);
            throw e;
        }
    }

    private void record(final MethodInvocation invocation, final CasMcpOperation.Outcome outcome) {
        val instance = auditor.getIfAvailable();
        if (instance != null) {
            instance.record(new CasMcpOperation(currentOperator(), operationName(invocation.getMethod()),
                arguments(invocation), outcome));
        }
    }

    private static String currentOperator() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && StringUtils.isNotBlank(authentication.getName())
            ? authentication.getName()
            : PrincipalResolver.UNKNOWN_USER;
    }

    private static String operationName(final Method method) {
        val tool = method.getAnnotation(McpTool.class);
        if (tool != null) {
            return StringUtils.defaultIfBlank(tool.name(), method.getName());
        }
        val resource = method.getAnnotation(McpResource.class);
        return resource != null ? StringUtils.defaultIfBlank(resource.uri(), method.getName()) : method.getName();
    }

    private static Map<String, String> arguments(final MethodInvocation invocation) {
        val parameters = invocation.getMethod().getParameters();
        val values = invocation.getArguments();
        val arguments = new LinkedHashMap<String, String>();
        for (var i = 0; i < parameters.length && i < values.length; i++) {
            if (values[i] != null) {
                arguments.put(parameters[i].getName(), StringUtils.abbreviate(String.valueOf(values[i]), MAX_ARGUMENT_LENGTH));
            }
        }
        return arguments;
    }
}
