package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.pac4j.core.client.BaseClient;

/**
 * This is {@link ConfigurableDelegatedClient}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ConfigurableDelegatedClient {
    private final BaseClient client;
    private Pac4jBaseClientProperties properties;
}
