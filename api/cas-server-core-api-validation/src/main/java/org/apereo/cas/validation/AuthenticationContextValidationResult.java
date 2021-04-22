package org.apereo.cas.validation;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link AuthenticationContextValidationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
@ToString
public class AuthenticationContextValidationResult implements Serializable {
    private static final long serialVersionUID = 5276264106164141194L;

    private final boolean success;

    @Builder.Default
    private final Optional<String> contextId = Optional.empty();
}
