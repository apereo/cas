package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.core.http.callback.QueryParameterCallbackUrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import java.util.List;

/**
 * This is {@link DelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface DelegatedIdentityProviderFactory extends DisposableBean {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(DelegatedIdentityProviderFactory.class);

    /**
     * The bean name that identifies the saml2 message factory instance.
     */
    String BEAN_NAME_SAML2_CLIENT_MESSAGE_FACTORY = "delegatedSaml2ClientSAMLMessageStoreFactory";

    /**
     * Build set of clients configured.
     *
     * @return the set
     */
    List<BaseClient> build();

    /**
     * Store.
     *
     * @param key            the key
     * @param currentClients the current clients
     */
    void store(String key, List<BaseClient> currentClients);

    /**
     * Retrieve list.
     *
     * @param key the key
     * @return the list
     */
    List<BaseClient> retrieve(String key);

    /**
     * Rebuild collection and invalidate the cached entries, if any.
     *
     * @return the collection
     */
    List<BaseClient> rebuild();

    /**
     * Build from properties.
     *
     * @param properties the properties
     * @return the list
     * @throws Exception the exception
     */
    List<BaseClient> buildFrom(CasConfigurationProperties properties) throws Exception;

    /**
     * Configure client name.
     *
     * @param client     the client
     * @param clientName the client name
     */
    static void configureClientName(final BaseClient client, final String clientName) {
        if (StringUtils.isNotBlank(clientName)) {
            client.setName(clientName);
        } else {
            val className = client.getClass().getSimpleName();
            val genName = className.concat(RandomUtils.randomNumeric(4));
            client.setName(genName);
            LOGGER.warn("Client name for [{}] is set to a generated value of [{}]. "
                + "Consider defining an explicit name for the delegated provider", className, genName);
        }
    }

    /**
     * Configure client custom properties.
     *
     * @param client           the client
     * @param clientProperties the client properties
     */
    static void configureClientCustomProperties(final BaseClient client, final Pac4jBaseClientProperties clientProperties) {
        val customProperties = client.getCustomProperties();
        customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_AUTO_REDIRECT_TYPE, clientProperties.getAutoRedirectType());

        FunctionUtils.doIfNotBlank(clientProperties.getPrincipalIdAttribute(),
            __ -> customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID, clientProperties.getPrincipalIdAttribute()));
        FunctionUtils.doIfNotBlank(clientProperties.getCssClass(),
            __ -> customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_CSS_CLASS, clientProperties.getCssClass()));
        FunctionUtils.doIfNotBlank(clientProperties.getDisplayName(),
            __ -> customProperties.put(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_DISPLAY_NAME, clientProperties.getDisplayName()));
    }


    /**
     * Configure client callback url.
     *
     * @param client           the client
     * @param clientProperties the client properties
     * @param defaultUrl       the default url
     */
    static void configureClientCallbackUrl(final BaseClient client,
                                           final Pac4jBaseClientProperties clientProperties,
                                           final String defaultUrl) {
        if (client instanceof final IndirectClient indirectClient) {
            val callbackUrl = StringUtils.defaultIfBlank(clientProperties.getCallbackUrl(), defaultUrl);
            indirectClient.setCallbackUrl(callbackUrl);
            LOGGER.trace("Client [{}] will use the callback URL [{}]", client.getName(), callbackUrl);
            val resolver = switch (clientProperties.getCallbackUrlType()) {
                case PATH_PARAMETER -> new PathParameterCallbackUrlResolver();
                case NONE -> new NoParameterCallbackUrlResolver();
                case QUERY_PARAMETER -> new QueryParameterCallbackUrlResolver();
            };
            indirectClient.setCallbackUrlResolver(resolver);
        }
    }
}
