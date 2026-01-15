package org.apereo.cas.token.authentication;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.Ordered;

/**
 * This is {@link TokenAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TokenAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private final ServicesManager servicesManager;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        FunctionUtils.doIfNotNull(transaction.getService(), service -> {
            val authentication = builder.build();
            val registeredService = servicesManager.findServiceBy(service);
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();
            val token = TokenAuthenticationSecurity.forRegisteredService(registeredService).generateTokenFor(authentication);
            builder.addAttribute(TokenConstants.PARAMETER_NAME_TOKEN, token);
        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
