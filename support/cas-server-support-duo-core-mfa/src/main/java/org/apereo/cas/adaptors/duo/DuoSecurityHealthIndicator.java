package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * This is {@link DuoSecurityHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class DuoSecurityHealthIndicator extends AbstractHealthIndicator {
    private final ApplicationContext applicationContext;

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        builder.withDetail("name", getClass().getSimpleName());
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(Objects::nonNull)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .forEach(p -> {
                val duoService = p.getDuoAuthenticationService();
                val result = duoService.ping();
                val b = builder.withDetail("duoApiHost", duoService.getApiHost());
                if (result) {
                    b.up();
                } else {
                    b.down();
                }
                b.build();
            });
    }
}
