package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.jose4j.jwk.JsonWebKeySet;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link OidcClientRegistrationUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
public class OidcClientRegistrationUtils {

    /**
     * Gets client registration response.
     *
     * @param registeredService the registered service
     * @param serverPrefix      the server prefix
     * @return the client registration response
     */
    public static OidcClientRegistrationResponse getClientRegistrationResponse(final OidcRegisteredService registeredService,
                                                                               final String serverPrefix) {
        val clientResponse = new OidcClientRegistrationResponse();
        clientResponse.setApplicationType(registeredService.getApplicationType());
        clientResponse.setClientId(registeredService.getClientId());
        clientResponse.setClientSecret(registeredService.getClientSecret());
        clientResponse.setSubjectType(registeredService.getSubjectType());
        clientResponse.setTokenEndpointAuthMethod(registeredService.getTokenEndpointAuthenticationMethod());
        clientResponse.setClientName(registeredService.getName());
        clientResponse.setRedirectUris(CollectionUtils.wrap(registeredService.getServiceId()));
        clientResponse.setUserInfoSignedResponseAlg(registeredService.getUserInfoSigningAlg());
        clientResponse.setUserInfoEncryptedResponseAlg(registeredService.getUserInfoEncryptedResponseAlg());
        clientResponse.setUserInfoEncryptedResponseEncoding(registeredService.getUserInfoEncryptedResponseEncoding());

        clientResponse.setContacts(
            registeredService.getContacts()
                .stream()
                .map(contact -> StringUtils.defaultIfBlank(contact.getEmail(), contact.getName()))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList())
        );
        clientResponse.setGrantTypes(
            Arrays.stream(OAuth20GrantTypes.values())
                .map(type -> type.getType().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.toList()));

        clientResponse.setResponseTypes(
            Arrays.stream(OAuth20ResponseTypes.values())
                .map(type -> type.getType().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.toList()));

        val validator = new SimpleUrlValidatorFactoryBean(false).getObject();
        val keystore = SpringExpressionLanguageValueResolver.getInstance().resolve(registeredService.getJwks());
        FunctionUtils.doUnchecked(param -> {
            if (Objects.requireNonNull(validator).isValid(keystore)) {
                clientResponse.setJwksUri(keystore);
            } else if (ResourceUtils.doesResourceExist(keystore)) {
                val res = ResourceUtils.getResourceFrom(keystore);
                String json;
                try (var in = res.getInputStream()) {
                    json = IOUtils.toString(in, StandardCharsets.UTF_8);
                }
                clientResponse.setJwks(new JsonWebKeySet(json).toJson());
            } else if (StringUtils.isNotBlank(keystore)) {
                val jwks = new JsonWebKeySet(keystore);
                clientResponse.setJwks(jwks.toJson());
            }
            clientResponse.setLogo(registeredService.getLogo());
            clientResponse.setPolicyUri(registeredService.getInformationUrl());
            clientResponse.setTermsOfUseUri(registeredService.getPrivacyUrl());
            clientResponse.setRedirectUris(CollectionUtils.wrapList(registeredService.getServiceId()));
            val clientConfigUri = getClientConfigurationUri(registeredService, serverPrefix);
            clientResponse.setRegistrationClientUri(clientConfigUri);
        });
        clientResponse.setClientSecretExpiresAt(registeredService.getClientSecretExpiration());
        val dynamicRegistrationPropName = RegisteredServiceProperty.RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION.getPropertyName();
        if (registeredService.getProperties().containsKey(dynamicRegistrationPropName)) {
            val dt = registeredService.getProperties()
                .get(RegisteredServiceProperties.OIDC_DYNAMIC_CLIENT_REGISTRATION_DATE.getPropertyName())
                .getValue(String.class);
            clientResponse.setClientIdIssuedAt(DateTimeUtils.localDateTimeOf(dt).toEpochSecond(ZoneOffset.UTC));
        }
        return clientResponse;
    }

    /**
     * Gets client configuration uri.
     *
     * @param registeredService the registered service
     * @param serverPrefix      the server prefix
     * @return the client configuration uri
     * @throws URISyntaxException the uri syntax exception
     */
    public static String getClientConfigurationUri(final OidcRegisteredService registeredService,
                                                   final String serverPrefix) throws URISyntaxException {
        return new URIBuilder(serverPrefix
            .concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_CONFIGURATION_URL))
            .addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .build()
            .toString();
    }
}
