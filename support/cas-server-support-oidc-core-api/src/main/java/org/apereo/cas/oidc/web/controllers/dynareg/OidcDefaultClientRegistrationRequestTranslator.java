package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.profile.OidcUserProfileSigningAndEncryptionService;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.hjson.JsonValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link OidcDefaultClientRegistrationRequestTranslator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcDefaultClientRegistrationRequestTranslator implements OidcClientRegistrationRequestTranslator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final int GENERATED_CLIENT_NAME_LENGTH = 8;

    private final ObjectProvider<OidcConfigurationContext> configurationContext;

    @Override
    public OidcRegisteredService translate(
        final OidcClientRegistrationRequest registrationRequest,
        final Optional<OidcRegisteredService> givenService) throws Exception {

        val context = configurationContext.getObject();

        val containsFragment = registrationRequest.getRedirectUris()
            .stream()
            .anyMatch(uri -> uri.contains("#"));
        if (containsFragment) {
            throw new IllegalArgumentException("Redirect URI cannot contain a fragment");
        }

        val servicesManager = context.getServicesManager();
        val registeredService = givenService.orElseGet(() -> registrationRequest.getRedirectUris()
            .stream()
            .map(uri -> (OidcRegisteredService) OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(servicesManager, uri))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(OidcRegisteredService::new));

        if (StringUtils.isNotBlank(registrationRequest.getClientName())) {
            registeredService.setName(registrationRequest.getClientName());
        } else if (StringUtils.isBlank(registeredService.getName())) {
            registeredService.setName(RandomUtils.randomAlphabetic(GENERATED_CLIENT_NAME_LENGTH));
        }

        val serviceId = String.join("|", registrationRequest.getRedirectUris());
        registeredService.setServiceId(serviceId);

        registeredService.setSectorIdentifierUri(registrationRequest.getSectorIdentifierUri());
        registeredService.setSubjectType(registrationRequest.getSubjectType());
        if (Strings.CI.equals(OidcSubjectTypes.PAIRWISE.getType(), registeredService.getSubjectType())) {
            registeredService.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());
        }

        if (StringUtils.isNotBlank(registrationRequest.getJwksUri())) {
            registeredService.setJwks(registrationRequest.getJwksUri());
        } else {
            val jwks = registrationRequest.getJwks();
            if (jwks != null && !jwks.getJsonWebKeys().isEmpty()) {
                jwks.getJsonWebKeys().stream()
                    .filter(key -> StringUtils.isBlank(key.getKeyId()))
                    .forEach(key -> key.setKeyId(RandomUtils.randomAlphabetic(6)));
                registeredService.setJwks(jwks.toJson());
            }
        }

        FunctionUtils.doIfNotBlank(registrationRequest.getTokenEndpointAuthMethod(),
            __ -> registeredService.setTokenEndpointAuthenticationMethod(registrationRequest.getTokenEndpointAuthMethod()));

        if (StringUtils.isBlank(registeredService.getClientId())) {
            registeredService.setClientId(context.getClientIdGenerator().getNewString());
        }
        if (StringUtils.isBlank(registeredService.getClientSecret())) {
            registeredService.setClientSecret(context.getClientSecretGenerator().getNewString());
        }
        registeredService.setEvaluationOrder(0);
        val urls = org.springframework.util.StringUtils.collectionToCommaDelimitedString(
            registrationRequest.getPostLogoutRedirectUris());
        registeredService.setLogoutUrl(urls);

        FunctionUtils.doIfNotBlank(registrationRequest.getLogo(), __ -> registeredService.setLogo(registrationRequest.getLogo()));
        FunctionUtils.doIfNotBlank(registrationRequest.getPolicyUri(), __ -> registeredService.setInformationUrl(registrationRequest.getPolicyUri()));
        FunctionUtils.doIfNotBlank(registrationRequest.getTermsOfUseUri(), __ -> registeredService.setPrivacyUrl(registrationRequest.getTermsOfUseUri()));

        processUserInfoSigningAndEncryption(registrationRequest, registeredService);
        processScopesAndResponsesAndGrants(registrationRequest, registeredService);

        if (!registrationRequest.getDefaultAcrValues().isEmpty()) {
            val multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
            multifactorPolicy.setMultifactorAuthenticationProviders(new HashSet<>(registrationRequest.getDefaultAcrValues()));
            registeredService.setMultifactorAuthenticationPolicy(multifactorPolicy);
        }

        processIdTokenSigningAndEncryption(registrationRequest, registeredService);
        processIntrospectionSigningAndEncryption(registrationRequest, registeredService);
        processContacts(registrationRequest, registeredService);
        processTlsClientAuthentication(registrationRequest, registeredService);
        processClientSecretExpiration(context, registeredService);
        processClientBackchannelAuthentication(registrationRequest, registeredService);

        registeredService.setDescription("Registered service ".concat(registeredService.getName()));
        validate(registrationRequest, registeredService);
        return registeredService;
    }

    private static void processClientBackchannelAuthentication(final OidcClientRegistrationRequest registrationRequest,
                                                               final OidcRegisteredService registeredService) {
        registeredService.setBackchannelTokenDeliveryMode(registrationRequest.getBackchannelTokenDeliveryMode());
        registeredService.setBackchannelUserCodeParameterSupported(registrationRequest.isBackchannelUserCodeParameterSupported());
        registeredService.setBackchannelClientNotificationEndpoint(registrationRequest.getBackchannelClientNotificationEndpoint());
        registeredService.setBackchannelAuthenticationRequestSigningAlg(registrationRequest.getBackchannelAuthenticationRequestSigningAlg());
    }

    private static void processTlsClientAuthentication(final OidcClientRegistrationRequest registrationRequest,
                                                       final OidcRegisteredService registeredService) {
        FunctionUtils.doIfNotNull(registrationRequest.getTlsClientAuthSanDns(), registeredService::setTlsClientAuthSanDns);
        FunctionUtils.doIfNotNull(registrationRequest.getTlsClientAuthSanEmail(), registeredService::setTlsClientAuthSanEmail);
        FunctionUtils.doIfNotNull(registrationRequest.getTlsClientAuthSanIp(), registeredService::setTlsClientAuthSanIp);
        FunctionUtils.doIfNotNull(registrationRequest.getTlsClientAuthSanUri(), registeredService::setTlsClientAuthSanUri);
        FunctionUtils.doIfNotNull(registrationRequest.getTlsClientAuthSubjectDn(), registeredService::setTlsClientAuthSubjectDn);
    }

    private void processScopesAndResponsesAndGrants(final OidcClientRegistrationRequest registrationRequest,
                                                    final OidcRegisteredService registeredService) {
        val context = configurationContext.getObject();
        FunctionUtils.doIfNotNull(registrationRequest.getGrantTypes(),
            __ -> registeredService.setSupportedGrantTypes(new HashSet<>(registrationRequest.getGrantTypes())));
        FunctionUtils.doIfNotNull(registrationRequest.getResponseTypes(),
            __ -> registeredService.setSupportedResponseTypes(new HashSet<>(registrationRequest.getResponseTypes())));

        val properties = context.getCasProperties();
        val supportedScopes = new HashSet<>(properties.getAuthn().getOidc().getDiscovery().getScopes());
        registeredService.setScopes(supportedScopes);
        if (registeredService.getSupportedGrantTypes().isEmpty()) {
            registeredService.setSupportedGrantTypes(CollectionUtils.wrapHashSet(OAuth20GrantTypes.AUTHORIZATION_CODE.getType()));
        }
        if (registeredService.getSupportedResponseTypes().isEmpty()) {
            registeredService.setSupportedResponseTypes(CollectionUtils.wrapHashSet(OAuth20ResponseTypes.CODE.getType()));
        }
    }

    private static void processUserInfoSigningAndEncryption(final OidcClientRegistrationRequest registrationRequest,
                                                            final OidcRegisteredService registeredService) {
        if (!Strings.CI.equals("none", registrationRequest.getUserInfoSignedResponseAlg())) {
            registeredService.setUserInfoSigningAlg(registrationRequest.getUserInfoSignedResponseAlg());
        }
        registeredService.setUserInfoEncryptedResponseAlg(registrationRequest.getUserInfoEncryptedResponseAlg());
        if (StringUtils.isNotBlank(registeredService.getUserInfoEncryptedResponseAlg())) {
            if (StringUtils.isBlank(registrationRequest.getUserInfoEncryptedResponseEncoding())) {
                registeredService.setUserInfoEncryptedResponseEncoding(
                    OidcUserProfileSigningAndEncryptionService.USER_INFO_RESPONSE_ENCRYPTION_ENCODING_DEFAULT);
            } else {
                registeredService.setUserInfoEncryptedResponseEncoding(registrationRequest.getUserInfoEncryptedResponseEncoding());
            }
        }
    }

    private static void processIntrospectionSigningAndEncryption(final OidcClientRegistrationRequest registrationRequest,
                                                                 final OidcRegisteredService registeredService) {
        FunctionUtils.doIfNotBlank(registrationRequest.getIntrospectionSignedResponseAlg(),
            __ -> registeredService.setIntrospectionSignedResponseAlg(registrationRequest.getIntrospectionSignedResponseAlg()));

        FunctionUtils.doIfNotBlank(registrationRequest.getIntrospectionEncryptedResponseAlg(),
            __ -> registeredService.setIntrospectionEncryptedResponseAlg(registrationRequest.getIntrospectionEncryptedResponseAlg()));

        FunctionUtils.doIfNotBlank(registrationRequest.getIntrospectionEncryptedResponseEncoding(),
            __ -> registeredService.setIntrospectionEncryptedResponseEncoding(registrationRequest.getIntrospectionEncryptedResponseEncoding()));
    }

    private static void processIdTokenSigningAndEncryption(final OidcClientRegistrationRequest registrationRequest,
                                                           final OidcRegisteredService registeredService) {
        if (StringUtils.isNotBlank(registrationRequest.getIdTokenSignedResponseAlg())) {
            registeredService.setIdTokenSigningAlg(registrationRequest.getIdTokenSignedResponseAlg());
            registeredService.setSignIdToken(true);
        }

        if (StringUtils.isNotBlank(registrationRequest.getIdTokenEncryptedResponseAlg())) {
            registeredService.setIdTokenEncryptionAlg(registrationRequest.getIdTokenEncryptedResponseAlg());
            registeredService.setEncryptIdToken(true);
        }

        if (StringUtils.isNotBlank(registrationRequest.getIdTokenEncryptedResponseEncoding())) {
            registeredService.setIdTokenEncryptionEncoding(registrationRequest.getIdTokenEncryptedResponseEncoding());
            registeredService.setEncryptIdToken(true);
        }
    }

    private static void processClientSecretExpiration(final OidcConfigurationContext context,
                                                      final OidcRegisteredService registeredService) {
        val clientSecretExp = Beans.newDuration(context.getCasProperties()
            .getAuthn().getOidc().getRegistration().getClientSecretExpiration()).toSeconds();
        if (clientSecretExp > 0 && registeredService.getClientSecretExpiration() <= 0) {
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC);
            val expirationDate = currentTime.plusSeconds(clientSecretExp);
            LOGGER.debug("Client secret shall expire at [{}] while now is [{}]", expirationDate, currentTime);
            registeredService.setClientSecretExpiration(expirationDate.toEpochSecond());
        }
    }

    private static void processContacts(final OidcClientRegistrationRequest registrationRequest,
                                        final OidcRegisteredService registeredService) {
        registeredService.getContacts().clear();
        registrationRequest.getContacts().forEach(givenContact -> {
            val contact = new DefaultRegisteredServiceContact();
            if (givenContact.contains("@")) {
                contact.setEmail(givenContact);
                contact.setName(givenContact.substring(0, givenContact.indexOf('@')));
            } else {
                contact.setName(givenContact);
            }
            registeredService.getContacts().add(contact);
        });
    }

    private void validate(final OidcClientRegistrationRequest registrationRequest,
                          final OidcRegisteredService registeredService) throws Exception {
        val context = configurationContext.getObject();
        if (StringUtils.isNotBlank(registeredService.getSectorIdentifierUri())) {
            HttpResponse sectorResponse = null;
            try {
                val exec = HttpExecutionRequest.builder()
                    .method(HttpMethod.GET)
                    .url(registeredService.getSectorIdentifierUri())
                    .build();
                sectorResponse = HttpUtils.execute(exec);
                if (sectorResponse != null && sectorResponse.getCode() == HttpStatus.SC_OK) {
                    try (val content = ((HttpEntityContainer) sectorResponse).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        val expectedType = MAPPER.getTypeFactory().constructParametricType(List.class, String.class);
                        val urls = MAPPER.readValue(JsonValue.readHjson(result).toString(), expectedType);
                        if (!urls.equals(registrationRequest.getRedirectUris())) {
                            throw new IllegalArgumentException("Invalid sector identifier uri");
                        }
                    }
                }
            } finally {
                HttpUtils.close(sectorResponse);
            }
        }

        val oidc = context.getCasProperties().getAuthn().getOidc();
        if (!oidc.getRegistration().getDynamicClientRegistrationMode().isProtected()
            && (StringUtils.isNotBlank(registrationRequest.getPolicyUri()) || StringUtils.isNotBlank(registrationRequest.getLogo()))) {
            val hosts = registrationRequest.getRedirectUris()
                .stream()
                .map(uri -> FunctionUtils.doUnchecked(() -> new URI(uri).getHost())).toList();
            if (StringUtils.isNotBlank(registrationRequest.getLogo())) {
                val logo = new URI(registrationRequest.getLogo()).getHost();
                if (!hosts.contains(logo)) {
                    throw new IllegalArgumentException("Invalid logo uri from an unknown host");
                }
            }

            if (StringUtils.isNotBlank(registrationRequest.getPolicyUri())) {
                val policy = new URI(registrationRequest.getPolicyUri()).getHost();
                if (!hosts.contains(policy)) {
                    throw new IllegalArgumentException("Invalid policy uri from an unknown host");
                }
            }
        }

        if (Strings.CI.equalsAny(registeredService.getBackchannelTokenDeliveryMode(),
            OidcBackchannelTokenDeliveryModes.PUSH.getMode(), OidcBackchannelTokenDeliveryModes.PING.getMode())) {
            Assert.hasText(registeredService.getBackchannelClientNotificationEndpoint(),
                "Backchannel client notification endpoint must be specified");
            Assert.isTrue(Strings.CI.startsWith(registeredService.getBackchannelClientNotificationEndpoint(), "https://"),
                "Backchannel client notification endpoint MUST be an HTTPS url");
        }
        
    }

}
