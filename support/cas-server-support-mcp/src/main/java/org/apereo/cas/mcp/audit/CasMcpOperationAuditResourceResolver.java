package org.apereo.cas.mcp.audit;

import module java.base;
import org.apereo.cas.util.text.MessageSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link CasMcpOperationAuditResourceResolver}, which records the operation and its arguments as the audited
 * resource of an MCP call, passed through the CAS message sanitizer so ticket ids and secrets are masked as in other
 * CAS audit records.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasMcpOperationAuditResourceResolver implements AuditResourceResolver {
    private final MessageSanitizer messageSanitizer;

    @Override
    public String[] resolveFrom(final JoinPoint target, final @Nullable Object returnValue) {
        return describe(returnValue instanceof final CasMcpOperation operation ? operation : firstArgument(target));
    }

    @Override
    public String[] resolveFrom(final JoinPoint target, final Exception exception) {
        return describe(firstArgument(target));
    }

    private static @Nullable CasMcpOperation firstArgument(final JoinPoint target) {
        val arguments = target.getArgs();
        return arguments != null && arguments.length > 0 && arguments[0] instanceof final CasMcpOperation operation ? operation : null;
    }

    private String[] describe(final @Nullable CasMcpOperation operation) {
        if (operation == null) {
            return new String[]{"unknown"};
        }
        return new String[]{messageSanitizer.sanitize("operation=" + operation.operation() + ", arguments=" + operation.arguments())};
    }
}
