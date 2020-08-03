package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceContact;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jose4j.jwk.JsonWebKeySet;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    @SneakyThrows
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
        clientResponse.setUserInfoSignedReponseAlg(registeredService.getUserInfoSigningAlg());
        clientResponse.setUserInfoEncryptedReponseAlg(registeredService.getUserInfoEncryptedResponseAlg());
        clientResponse.setUserInfoEncryptedReponseEncoding(registeredService.getUserInfoEncryptedResponseEncoding());

        clientResponse.setContacts(
            registeredService.getContacts()
                .stream()
                .map(RegisteredServiceContact::getName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList())
        );
        clientResponse.setGrantTypes(
            Arrays.stream(OAuth20GrantTypes.values())
                .map(type -> type.getType().toLowerCase())
                .collect(Collectors.toList()));

        clientResponse.setResponseTypes(
            Arrays.stream(OAuth20ResponseTypes.values())
                .map(type -> type.getType().toLowerCase())
                .collect(Collectors.toList()));

        val validator = new SimpleUrlValidatorFactoryBean(false).getObject();
        val keystore = registeredService.getJwks();
        if (Objects.requireNonNull(validator).isValid(keystore)) {
            clientResponse.setJwksUri(keystore);
        } else if (ResourceUtils.doesResourceExist(keystore)) {
            val res = ResourceUtils.getResourceFrom(keystore);
            val json = IOUtils.toString(res.getInputStream(), StandardCharsets.UTF_8);
            clientResponse.setJwks(new JsonWebKeySet(json).toJson());
        } else {
            val jwks = new JsonWebKeySet(keystore);
            clientResponse.setJwks(jwks.toJson());
        }
        clientResponse.setLogo(registeredService.getLogo());
        clientResponse.setPolicyUri(registeredService.getInformationUrl());
        clientResponse.setTermsOfUseUri(registeredService.getPrivacyUrl());
        clientResponse.setRedirectUris(CollectionUtils.wrapList(registeredService.getServiceId()));
        val clientConfigUri = getClientConfigurationUri(registeredService, serverPrefix);
        clientResponse.setRegistrationClientUri(clientConfigUri);
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
            .addParameter(OidcConstants.CLIENT_REGISTRATION_CLIENT_ID, registeredService.getClientId())
            .build()
            .toString();
    }
}
