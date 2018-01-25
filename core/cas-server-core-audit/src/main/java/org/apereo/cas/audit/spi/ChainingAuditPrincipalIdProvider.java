package org.apereo.cas.audit.spi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ChainingAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Slf4j
public class ChainingAuditPrincipalIdProvider implements AuditPrincipalIdProvider {
    private int order = Integer.MAX_VALUE;

    private List<AuditPrincipalIdProvider> providers = new ArrayList<>();

    /**
     * Add provider.
     *
     * @param provider the provider
     */
    public void addProvider(final AuditPrincipalIdProvider provider) {
        providers.add(provider);
    }

    @Override
    public String getPrincipalIdFrom(final Authentication authentication, final Object resultValue, final Exception exception) {
        AnnotationAwareOrderComparator.sort(this.providers);
        final AuditPrincipalIdProvider result = providers.stream()
            .filter(p -> p.supports(authentication, resultValue, exception))
            .findFirst()
            .orElse(new DefaultAuditPrincipalIdProvider());
        return result.getPrincipalIdFrom(authentication, resultValue, exception);
    }

    @Override
    public boolean supports(final Authentication authentication, final Object resultValue, final Exception exception) {
        return true;
    }
}
