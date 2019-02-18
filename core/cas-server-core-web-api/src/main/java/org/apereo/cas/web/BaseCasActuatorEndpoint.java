package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.RequiredArgsConstructor;

/**
 * This is {@link BaseCasActuatorEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public abstract class BaseCasActuatorEndpoint {
    /**
     * The CAS properties.
     */
    protected final CasConfigurationProperties casProperties;
}
