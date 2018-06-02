package org.apereo.cas.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;

/**
 * This is {@link BaseCasMvcEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class BaseCasMvcEndpoint {
    /**
     * The Cas properties.
     */
    private final CasConfigurationProperties casProperties;
}
