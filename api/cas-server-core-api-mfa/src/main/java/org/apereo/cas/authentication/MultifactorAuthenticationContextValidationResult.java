package org.apereo.cas.authentication;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

/**
 * This is {@link MultifactorAuthenticationContextValidationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
public class MultifactorAuthenticationContextValidationResult {
    private final boolean success;

    @Builder.Default
    private final Optional<MultifactorAuthenticationProvider> provider = Optional.empty();

}
