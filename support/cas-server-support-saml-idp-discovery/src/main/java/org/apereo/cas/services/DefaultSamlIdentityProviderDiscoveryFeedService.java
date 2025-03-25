package org.apereo.cas.services;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.util.InitializableObject;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultSamlIdentityProviderDiscoveryFeedService}.
 *
 * @author Sam Hough
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class DefaultSamlIdentityProviderDiscoveryFeedService implements SamlIdentityProviderDiscoveryFeedService {

    private final CasConfigurationProperties casProperties;

    private final List<SamlIdentityProviderEntityParser> parsers;

    private final DelegatedIdentityProviders identityProviders;

    private final ArgumentExtractor argumentExtractor;

    private final List<DelegatedClientIdentityProviderAuthorizer> authorizers;

    @Override
    public Collection<SamlIdentityProviderEntity> getDiscoveryFeed() {
        return parsers
            .stream()
            .map(SamlIdentityProviderEntityParser::getIdentityProviders)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getEntityIds() {
        return identityProviders.findAllClients()
            .stream()
            .filter(SAML2Client.class::isInstance)
            .map(SAML2Client.class::cast)
            .peek(InitializableObject::init)
            .map(SAML2Client::getIdentityProviderResolvedEntityId)
            .collect(Collectors.toList());
    }

    @Override
    public DelegatedClientIdentityProviderConfiguration getProvider(final String entityID,
                                                                    final HttpServletRequest httpServletRequest,
                                                                    final HttpServletResponse httpServletResponse) {
        val idp = getDiscoveryFeed()
            .stream()
            .filter(entity -> entity.getEntityID().equals(entityID))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No identity provider found for discovery feed's entity ID: " + entityID));
        val samlClient = identityProviders.findAllClients()
            .stream()
            .filter(SAML2Client.class::isInstance)
            .map(SAML2Client.class::cast)
            .peek(InitializableObject::init)
            .filter(c -> c.getIdentityProviderResolvedEntityId().equalsIgnoreCase(idp.getEntityID()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No SAML identity provider found for entity ID: " + entityID));

        val webContext = new JEEContext(httpServletRequest, httpServletResponse);
        val service = argumentExtractor.extractService(httpServletRequest);

        val authorized = authorizers
            .stream()
            .allMatch(Unchecked.predicate(authz -> authz.isDelegatedClientAuthorizedForService(samlClient, service, httpServletRequest)));

        if (authorized) {
            val provider = DelegatedClientIdentityProviderConfigurationFactory.builder()
                .service(service)
                .client(samlClient)
                .webContext(webContext)
                .casProperties(casProperties)
                .build()
                .resolve();

            if (provider.isPresent()) {
                return provider.get();
            }
        }
        throw UnauthorizedServiceException.denied("Denied: %s".formatted(entityID));
    }
}
