package org.apereo.cas.validation;

import org.apereo.cas.services.CasModelRegisteredService;

import lombok.val;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CasProtocolVersionValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class CasProtocolVersionValidationSpecification implements CasProtocolValidationSpecification {
    private final Set<String> supportedVersions;

    public CasProtocolVersionValidationSpecification(final Set<CasProtocolVersions> supportedVersions) {
        this.supportedVersions = supportedVersions.stream().map(CasProtocolVersions::name).collect(Collectors.toSet());
    }

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        val registeredService = assertion.registeredService();
        if (registeredService instanceof CasModelRegisteredService casService) {
            return casService.getSupportedProtocols().isEmpty()
                   || casService.getSupportedProtocols().containsAll(supportedVersions);
        }
        return false;
    }
}
