package org.apereo.cas.validation;

import org.apereo.cas.services.CasModelRegisteredService;
import org.apereo.cas.services.CasProtocolVersions;
import lombok.RequiredArgsConstructor;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * This is {@link CasProtocolVersionValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class CasProtocolVersionValidationSpecification implements CasProtocolValidationSpecification {
    private final Set<CasProtocolVersions> supportedVersions;

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        val registeredService = assertion.getRegisteredService();
        if (registeredService instanceof final CasModelRegisteredService casService) {
            return casService.getSupportedProtocols().isEmpty()
                || casService.getSupportedProtocols().containsAll(supportedVersions);
        }
        return true;
    }
}
