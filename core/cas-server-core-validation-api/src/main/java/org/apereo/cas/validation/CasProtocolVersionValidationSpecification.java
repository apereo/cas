package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.CasProtocolVersions;
import lombok.RequiredArgsConstructor;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link CasProtocolVersionValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class CasProtocolVersionValidationSpecification implements CasProtocolValidationSpecification {
    private final Set<CasProtocolVersions> supportedVersions;
    private final TenantExtractor tenantExtractor;

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        val tenantSupported = tenantExtractor.extract(request)
            .stream()
            .filter(tenant -> Objects.nonNull(tenant.getAuthenticationPolicy().getAuthenticationProtocolPolicy()))
            .map(tenant -> tenant.getAuthenticationPolicy().getAuthenticationProtocolPolicy())
            .filter(policy -> Objects.nonNull(policy.getSupportedProtocols()))
            .map(policy -> {
                val supportedProtocols = policy.getSupportedProtocols()
                    .stream()
                    .map(version -> CasProtocolVersions.valueOf(version.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toSet());
                return supportedProtocols.isEmpty() || supportedProtocols.containsAll(supportedVersions);
            })
            .findAny()
            .orElse(Boolean.TRUE);

        val registeredService = assertion.getRegisteredService();
        if (tenantSupported && registeredService instanceof final CasModelRegisteredService casService) {
            val supportedProtocols = casService.getSupportedProtocols();
            return supportedProtocols.isEmpty() || supportedProtocols.containsAll(supportedVersions);
        }
        return tenantSupported;
    }
}
