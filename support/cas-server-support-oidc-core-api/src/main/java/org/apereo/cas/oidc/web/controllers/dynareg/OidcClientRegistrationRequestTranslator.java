package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.profile.OidcUserProfileSigningAndEncryptionService;
import org.apereo.cas.services.DefaultRegisteredServiceContact;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link OidcClientRegistrationRequestTranslator}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcClientRegistrationRequestTranslator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final int GENERATED_CLIENT_NAME_LENGTH = 8;

    private final OidcConfigurationContext context;

    /**
     * Translate request into a response and store the service.
     *
     * @param registrationRequest the registration request
     * @param givenService        the given service
     * @return the service
     * @throws Exception the exception
     */
    public OidcRegisteredService translate(
        final OidcClientRegistrationRequest registrationRequest,
        final Optional<OidcRegisteredService> givenService) throws Exception {

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
        if (StringUtils.equalsIgnoreCase(OidcSubjectTypes.PAIRWISE.getType(), registeredService.getSubjectType())) {
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
        if (StringUtils.isNotBlank(registrationRequest.getTokenEndpointAuthMethod())) {
            registeredService.setTokenEndpointAuthenticationMethod(registrationRequest.getTokenEndpointAuthMethod());
        }

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

        if (StringUtils.isNotBlank(registrationRequest.getLogo())) {
            registeredService.setLogo(registrationRequest.getLogo());
        }
        if (StringUtils.isNotBlank(registrationRequest.getPolicyUri())) {
            registeredService.setInformationUrl(registrationRequest.getPolicyUri());
        }
        if (StringUtils.isNotBlank(registrationRequest.getTermsOfUseUri())) {
            registeredService.setPrivacyUrl(registrationRequest.getTermsOfUseUri());
        }

        if (!StringUtils.equalsIgnoreCase("none", registrationRequest.getUserInfoSignedReponseAlg())) {
            registeredService.setUserInfoSigningAlg(registrationRequest.getUserInfoSignedReponseAlg());
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

        val properties = context.getCasProperties();
        val supportedScopes = new HashSet<>(properties.getAuthn().getOidc().getDiscovery().getScopes());
        registeredService.setScopes(supportedScopes);
        val processedScopes = new LinkedHashSet<>(supportedScopes);
        registeredService.setScopes(processedScopes);

        if (!registrationRequest.getDefaultAcrValues().isEmpty()) {
            val multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
            multifactorPolicy.setMultifactorAuthenticationProviders(new HashSet<>(registrationRequest.getDefaultAcrValues()));
            registeredService.setMultifactorAuthenticationPolicy(multifactorPolicy);
        }

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

        registeredService.getContacts().clear();
        registrationRequest.getContacts().forEach(c -> {
            val contact = new DefaultRegisteredServiceContact();
            if (c.contains("@")) {
                contact.setEmail(c);
                contact.setName(c.substring(0, c.indexOf('@')));
            } else {
                contact.setName(c);
            }
            registeredService.getContacts().add(contact);
        });
        val clientSecretExp = Beans.newDuration(context.getCasProperties()
            .getAuthn().getOidc().getRegistration().getClientSecretExpiration()).toSeconds();
        if (clientSecretExp > 0 && registeredService.getClientSecretExpiration() <= 0) {
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC);
            val expirationDate = currentTime.plusSeconds(clientSecretExp);
            LOGGER.debug("Client secret shall expire at [{}] while now is [{}]", expirationDate, currentTime);
            registeredService.setClientSecretExpiration(expirationDate.toEpochSecond());
        }

        registeredService.setDescription("Registered service ".concat(registeredService.getName()));
        validate(registrationRequest, registeredService);
        return registeredService;
    }

    private void validate(final OidcClientRegistrationRequest registrationRequest,
                          final OidcRegisteredService registeredService) throws Exception {
        if (StringUtils.isNotBlank(registeredService.getSectorIdentifierUri())) {
            HttpResponse sectorResponse = null;
            try {
                val exec = HttpUtils.HttpExecutionRequest.builder()
                    .method(HttpMethod.GET)
                    .url(registeredService.getSectorIdentifierUri())
                    .build();
                sectorResponse = HttpUtils.execute(exec);
                if (sectorResponse != null && sectorResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    val result = IOUtils.toString(sectorResponse.getEntity().getContent(), StandardCharsets.UTF_8);
                    val urls = MAPPER.readValue(JsonValue.readHjson(result).toString(), List.class);
                    if (!urls.equals(registrationRequest.getRedirectUris())) {
                        throw new IllegalArgumentException("Invalid sector identifier uri");
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
    }

}
