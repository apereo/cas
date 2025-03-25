package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.util.NamedObject;
import org.pac4j.core.client.BaseClient;
import java.util.List;

/**
 * This is {@link ConfigurableDelegatedClient}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
public interface ConfigurableDelegatedClientBuilder extends NamedObject {


    /**
     * Supply list of clients.
     *
     * @return the list
     * @throws Exception the exception
     */
    List<ConfigurableDelegatedClient> build(CasConfigurationProperties casProperties) throws Exception;

    /**
     * Configure base client.
     *
     * @param client           the prepared client
     * @param clientProperties the client properties
     * @param properties       the properties
     * @return the base client
     * @throws Exception the exception
     */
    default List<? extends BaseClient> configure(final BaseClient client,
                                                 final Pac4jBaseClientProperties clientProperties,
                                                 final CasConfigurationProperties properties) throws Exception {
        return List.of(client);
    }
}
