package org.apereo.cas.adaptors.duo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.services.VariegatedMultifactorAuthenticationProvider;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * This is {@link DuoSecurityHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DuoSecurityHealthIndicator extends AbstractHealthIndicator {
    private final VariegatedMultifactorAuthenticationProvider duoMultifactorAuthenticationProvider;

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        duoMultifactorAuthenticationProvider.getProviders()
            .stream()
            .filter(DuoMultifactorAuthenticationProvider.class::isInstance)
            .map(DuoMultifactorAuthenticationProvider.class::cast)
            .forEach(p -> {
                final var result = p.getDuoAuthenticationService().ping();
                final var b = builder.withDetail("duoApiHost", p.getDuoAuthenticationService().getApiHost());
                if (result) {
                    b.up().build();
                } else {
                    b.down().build();
                }
            });
    }
}
