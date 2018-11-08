package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link BaseCasMvcEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@RequiredArgsConstructor
public abstract class BaseCasMvcEndpoint {
    /**
     * The Cas properties.
     */
    private final CasConfigurationProperties casProperties;
}
