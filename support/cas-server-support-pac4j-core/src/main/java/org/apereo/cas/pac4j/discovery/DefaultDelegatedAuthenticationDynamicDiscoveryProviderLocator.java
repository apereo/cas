package org.apereo.cas.pac4j.discovery;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator implements DelegatedAuthenticationDynamicDiscoveryProviderLocator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final DelegatedClientIdentityProviderConfigurationProducer providerProducer;

    private final DelegatedIdentityProviders identityProviders;

    private final PrincipalResolver principalResolver;

    private final PrincipalFactory clientPrincipalFactory;

    private final CasConfigurationProperties properties;

    @Override
    public Optional<IndirectClient> locate(final DynamicDiscoveryProviderRequest request, final WebContext webContext) throws Throwable {
        val resource = properties.getAuthn().getPac4j().getCore().getDiscoverySelection().getJson().getLocation();
        val mappings = MAPPER.readValue(resource.getInputStream(),
            new TypeReference<Map<String, DelegatedAuthenticationDynamicDiscoveryProvider>>() {
            });
        val principal = resolvePrincipal(request);
        LOGGER.debug("Resolved principal to be [{}]", principal);
        return mappings
            .entrySet()
            .stream()
            .sorted(Comparator.comparingInt(o -> o.getValue().getOrder()))
            .map(entry -> getMatchingProvider(principal, entry.getKey(), entry.getValue()))
            .filter(Objects::nonNull)
            .map(provider -> identityProviders.findClient(provider.getClientName(), webContext))
            .flatMap(Optional::stream)
            .map(IndirectClient.class::cast)
            .findFirst();
    }

    protected Principal resolvePrincipal(final DynamicDiscoveryProviderRequest request) throws Throwable {
        val userId = request.getUserId();
        val resolvedPrincipal = principalResolver.resolve(new BasicIdentifiableCredential(userId));

        if (resolvedPrincipal instanceof NullPrincipal) {
            LOGGER.debug("No principal was resolved. Falling back to the username [{}] from the credentials.", userId);
            return clientPrincipalFactory.createPrincipal(userId);
        }

        return resolvedPrincipal;
    }

    protected DelegatedAuthenticationDynamicDiscoveryProvider getMatchingProvider(
        final Principal principal, final String keyPattern,
        final DelegatedAuthenticationDynamicDiscoveryProvider provider) {
        val attrName = properties.getAuthn().getPac4j().getCore().getDiscoverySelection().getJson().getPrincipalAttribute();
        if (StringUtils.isNotBlank(attrName) && principal.getAttributes().containsKey(attrName)) {
            val attrValues = principal.getAttributes().get(attrName);
            LOGGER.debug("Checking attribute values [{}] against [{}]", attrValues, keyPattern);
            return attrValues.stream().anyMatch(value -> RegexUtils.find(keyPattern, value.toString())) ? provider : null;
        }
        return RegexUtils.find(keyPattern, principal.getId()) ? provider : null;
    }
}
