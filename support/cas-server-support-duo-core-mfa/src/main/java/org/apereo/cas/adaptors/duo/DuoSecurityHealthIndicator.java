package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.DuoMultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * This is {@link DuoSecurityHealthIndicator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class DuoSecurityHealthIndicator extends AbstractHealthIndicator {

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        val context = ApplicationContextProvider.getApplicationContext();
        val providers = context.getBeansOfType(DuoMultifactorAuthenticationProvider.class).values();
        providers
            .stream()
            .filter(DuoMultifactorAuthenticationProvider.class::isInstance)
            .map(DuoMultifactorAuthenticationProvider.class::cast)
            .forEach(p -> {
                val result = p.getDuoAuthenticationService().ping();
                val b = builder.withDetail("duoApiHost", p.getDuoAuthenticationService().getApiHost());
                if (result) {
                    b.up().build();
                } else {
                    b.down().build();
                }
            });
    }
}
