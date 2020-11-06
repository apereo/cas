package org.apereo.cas.qr.validation;

import org.apereo.cas.services.RegisteredService;

import java.util.Optional;

/**
 * This is {@link QRAuthenticationTokenValidatorService}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface QRAuthenticationTokenValidatorService {

    /**
     * Validate.
     *
     * @param service the service
     * @param token   the token
     * @return validation result
     */
    QRAuthenticationTokenValidationResult validate(Optional<RegisteredService> service, String token);
}
