/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.pac4j;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the pac4j configuration.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Configuration
public class Pac4jConfiguration {

    @Value("${server.prefix:http://localhost:8080/cas}/login")
    private String serverLoginUrl;

    @Value("${cas.pac4j.facebook.id:}")
    private String facebookId;
    @Value("${cas.pac4j.facebook.secret:}")
    private String facebookSecret;
    @Value("${cas.pac4j.facebook.scope:}")
    private String facebookScope;
    @Value("${cas.pac4j.facebook.fields:}")
    private String facebookFields;

    @Value("${cas.pac4j.twitter.id:}")
    private String twitterId;
    @Value("${cas.pac4j.twitter.secret:}")
    private String twitterSecret;

    @Value("${cas.pac4j.saml.keystorePassword:}")
    private String samlKeystorePassword;
    @Value("${cas.pac4j.saml.privateKeyPassword:}")
    private String samlPrivateKeyPassword;
    @Value("${cas.pac4j.saml.keystorePath:}")
    private String samlKeystorePath;
    @Value("${cas.pac4j.saml.identityProviderMetadataPath:}")
    private String samlIdentityProviderMetadataPath;
    @Value("${cas.pac4j.saml.maximumAuthenticationLifetime:}")
    private String samlMaximumAuthenticationLifetime;
    @Value("${cas.pac4j.saml.serviceProviderEntityId:}")
    private String samlServiceProviderEntityId;
    @Value("${cas.pac4j.saml.serviceProviderMetadataPath:}")
    private String samlServiceProviderMetadataPath;

    @Autowired(required = false)
    @Qualifier("clients")
    private Clients clients;

    /**
     * Returning the built clients.
     *
     * @return the built clients.
     */
    @Bean(name = "builtClients")
    public Clients clients() {
        String callbackUrl = serverLoginUrl;
        List<Client> allClients = new ArrayList<>();
        // we already have a global Clients configuration defined in a Spring context
        if (clients != null) {
            final String clientsCallbackUrl = clients.getCallbackUrl();
            if (StringUtils.isNotBlank(clientsCallbackUrl)) {
                callbackUrl = clientsCallbackUrl;
            }
            allClients = clients.findAllClients();
        }

        // add new clients by properties
        autoCreateFacebookClient(allClients);
        autoCreateTwitterClient(allClients);
        autoCreateSaml2Client(allClients);

        // rebuild a new Clients configuration
        if (allClients == null || allClients.size() == 0) {
            throw new IllegalArgumentException("At least one pac4j client must be defined");
        }
        return new Clients(callbackUrl, allClients);
    }

    private void autoCreateFacebookClient(final List<Client> clients) {
        if (StringUtils.isNotBlank(facebookId) && StringUtils.isNotBlank(facebookSecret)) {
            final FacebookClient facebookClient = new FacebookClient(facebookId, facebookSecret);
            if (StringUtils.isNotBlank(facebookScope)) {
                facebookClient.setScope(facebookScope);
            }
            if (StringUtils.isNotBlank(facebookFields)) {
                facebookClient.setFields(facebookFields);
            }
            clients.add(facebookClient);
        }
    }

    private void autoCreateTwitterClient(final List<Client> clients) {
        if (StringUtils.isNotBlank(twitterId) && StringUtils.isNotBlank(twitterSecret)) {
            final TwitterClient twitterClient = new TwitterClient(twitterId, twitterSecret);
            clients.add(twitterClient);
        }
    }

    private void autoCreateSaml2Client(final List<Client> clients) {
        if (StringUtils.isNotBlank(samlKeystorePassword) && StringUtils.isNotBlank(samlPrivateKeyPassword)
                && StringUtils.isNotBlank(samlKeystorePath) && StringUtils.isNotBlank(samlIdentityProviderMetadataPath)) {
            final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration(samlKeystorePath, samlKeystorePassword,
                    samlPrivateKeyPassword, samlIdentityProviderMetadataPath);
            if (StringUtils.isNotBlank(samlMaximumAuthenticationLifetime)) {
                cfg.setMaximumAuthenticationLifetime(Integer.parseInt(samlMaximumAuthenticationLifetime));
            }
            if (StringUtils.isNotBlank(samlServiceProviderEntityId)) {
                cfg.setServiceProviderEntityId(samlServiceProviderEntityId);
            }
            if (StringUtils.isNotBlank(samlServiceProviderMetadataPath)) {
                cfg.setServiceProviderMetadataPath(samlServiceProviderMetadataPath);
            }
            final SAML2Client saml2Client = new SAML2Client(cfg);
            clients.add(saml2Client);
        }
    }
}
