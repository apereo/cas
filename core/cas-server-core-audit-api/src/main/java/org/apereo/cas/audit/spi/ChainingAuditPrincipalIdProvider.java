package org.apereo.cas.audit.spi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;

import java.util.List;

/**
 * This is {@link ChainingAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Slf4j
@RequiredArgsConstructor
public class ChainingAuditPrincipalIdProvider implements AuditPrincipalIdProvider {
    private int order = Integer.MAX_VALUE;

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
        final AuditPrincipalIdProvider result = providers.stream()
            .filter(p -> p.supports(authentication, resultValue, exception))
            .findFirst()
            .orElseGet(DefaultAuditPrincipalIdProvider::new);
        return result.getPrincipalIdFrom(authentication, resultValue, exception);
    }

    @Override
    public boolean supports(final Authentication authentication, final Object resultValue, final Exception exception) {
        return true;
    }
}
