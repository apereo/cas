package org.apereo.cas.qr.validation;

import org.apereo.cas.services.RegisteredService;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link QRAuthenticationTokenValidationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@SuperBuilder
public class QRAuthenticationTokenValidationRequest implements Serializable {
    private static final long serialVersionUID = -2010576443419962855L;

    private final String token;

    private final String deviceId;

    @Builder.Default
    private final Optional<RegisteredService> registeredService = Optional.empty();
}
