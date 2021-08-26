package org.apereo.cas.qr.validation;

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
     * @param request the request
     * @return validation result
     */
    QRAuthenticationTokenValidationResult validate(QRAuthenticationTokenValidationRequest request);
}
