package org.apereo.cas.validation;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

/**
 * This is {@link AuthenticationContextValidationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
public class AuthenticationContextValidationResult {
    private final boolean success;

    private final Optional<String> providerId;
}
