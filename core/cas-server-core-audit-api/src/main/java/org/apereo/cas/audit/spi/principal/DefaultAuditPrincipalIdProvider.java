package org.apereo.cas.audit.spi.principal;

import module java.base;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DefaultAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public class DefaultAuditPrincipalIdProvider implements AuditPrincipalIdProvider {
    private int order = Integer.MAX_VALUE;

    @Override
    public @Nullable String getPrincipalIdFrom(final JoinPoint auditTarget,
                                               @Nullable final Authentication authentication,
                                               @Nullable final Object returnValue,
                                               @Nullable final Exception exception) {
        return Optional.ofNullable(authentication)
            .map(authn -> authn.getPrincipal().getId())
            .orElse(null);
    }

    @Override
    public boolean supports(final JoinPoint auditTarget,
                            @Nullable final Authentication authentication,
                            @Nullable final Object resultValue,
                            @Nullable final Exception exception) {
        return authentication != null;
    }
}
