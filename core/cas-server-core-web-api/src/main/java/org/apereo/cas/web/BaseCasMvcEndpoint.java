package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.RequiredArgsConstructor;

/**
 * This is {@link BaseCasMvcEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public abstract class BaseCasMvcEndpoint {
    /**
     * The CAS properties.
     */
    protected final CasConfigurationProperties casProperties;
}
