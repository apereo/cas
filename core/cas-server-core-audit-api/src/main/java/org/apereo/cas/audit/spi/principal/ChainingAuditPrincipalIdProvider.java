package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

/**
 * This is {@link ChainingAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@RequiredArgsConstructor
public class ChainingAuditPrincipalIdProvider implements AuditPrincipalIdProvider {
    private final List<AuditPrincipalIdProvider> providers;

    /**
     * Add provider.
     *
     * @param provider the provider
     */
    public void addProvider(final AuditPrincipalIdProvider provider) {
        providers.add(provider);
    }

    /**
     * Add providers.
     *
     * @param provider the provider
     */
    public void addProviders(final List<AuditPrincipalIdProvider> provider) {
        providers.addAll(provider);
    }

    @Override
    public String getPrincipalIdFrom(final Authentication authentication, final Object resultValue, final Exception exception) {
        val result = providers.stream()
            .filter(p -> p.supports(authentication, resultValue, exception))
            .findFirst()
            .orElseGet(DefaultAuditPrincipalIdProvider::new);
        return result.getPrincipalIdFrom(authentication, resultValue, exception);
    }

    @Override
    public boolean supports(final Authentication authentication, final Object resultValue, final Exception exception) {
        return true;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
