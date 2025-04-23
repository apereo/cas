package org.apereo.cas.okta;

import org.apereo.cas.configuration.model.support.okta.BaseOktaApiProperties;
import org.apereo.cas.configuration.model.support.okta.OktaAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.commons.http.config.Proxy;
import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.client.AuthorizationMode;
import com.okta.sdk.client.Client;
import com.okta.sdk.client.Clients;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link OktaConfigurationFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@UtilityClass
public class OktaConfigurationFactory {

    /**
     * Build client.
     *
     * @param properties the properties
     * @return the client
     */
    public static Client buildClient(final BaseOktaApiProperties properties) {
        val clientBuilder = Clients.builder()
            .setOrgUrl(properties.getOrganizationUrl())
            .setConnectionTimeout(properties.getConnectionTimeout());

        FunctionUtils.doIfNotNull(properties.getApiToken(), token -> clientBuilder.setClientCredentials(new TokenClientCredentials(token)));
        FunctionUtils.doIfNotNull(properties.getPrivateKey().getLocation(), path -> {
            val resource = IOUtils.toString(path.getInputStream(), StandardCharsets.UTF_8);
            clientBuilder
                .setAuthorizationMode(AuthorizationMode.PRIVATE_KEY)
                .setPrivateKey(resource)
                .setClientId(properties.getClientId())
                .setScopes(CollectionUtils.wrapHashSet(properties.getScopes()));
        });

        if (StringUtils.isNotBlank(properties.getProxyHost()) && properties.getProxyPort() > 0) {
            if (StringUtils.isNotBlank(properties.getProxyUsername()) && StringUtils.isNotBlank(properties.getProxyPassword())) {
                clientBuilder.setProxy(new Proxy(properties.getProxyHost(), properties.getProxyPort(),
                    properties.getProxyUsername(), properties.getProxyPassword()));
            } else {
                clientBuilder.setProxy(new Proxy(properties.getProxyHost(), properties.getProxyPort()));
            }
        }
        return clientBuilder.build();
    }

    /**
     * Build authentication client.
     *
     * @param properties the properties
     * @return the authentication client
     */
    public static AuthenticationClient buildAuthenticationClient(final OktaAuthenticationProperties properties) {
        val clientBuilder = AuthenticationClients.builder()
            .setOrgUrl(properties.getOrganizationUrl())
            .setConnectionTimeout(properties.getConnectionTimeout());
        
        if (StringUtils.isNotBlank(properties.getProxyHost()) && properties.getProxyPort() > 0) {
            if (StringUtils.isNotBlank(properties.getProxyUsername()) && StringUtils.isNotBlank(properties.getProxyPassword())) {
                clientBuilder.setProxy(new Proxy(properties.getProxyHost(), properties.getProxyPort(),
                    properties.getProxyUsername(), properties.getProxyPassword()));
            } else {
                clientBuilder.setProxy(new Proxy(properties.getProxyHost(), properties.getProxyPort()));
            }
        }
        return clientBuilder.build();
    }
}
