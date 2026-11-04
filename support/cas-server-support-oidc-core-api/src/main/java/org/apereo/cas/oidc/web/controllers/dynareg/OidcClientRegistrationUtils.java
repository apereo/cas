package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.net.URIBuilder;
import org.jose4j.jwk.JsonWebKeySet;

/**
 * This is {@link OidcClientRegistrationUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
public class OidcClientRegistrationUtils {
    private static final String QUOTE_START = "\\Q";

    private static final String QUOTE_END = "\\E";

    private static final String ESCAPED_QUOTE_END = "\\\\E";

    private static final String SERVICE_ID_PREFIX = "^(";

    private static final String SERVICE_ID_SUFFIX = ")$";

    /**
     * The service id of a client registered with the given redirect URIs: a regular expression that matches each of them
     * exactly and nothing else. Joining the URIs as they are would let {@code .} match any character, so that a look-alike
     * host matches, and {@code ?} make the preceding character optional, so that a URI with a query never matches itself.
     *
     * @param redirectUris the redirect URIs
     * @return the service id
     */
    public static String toServiceId(final Collection<String> redirectUris) {
        return redirectUris
            .stream()
            .map(Pattern::quote)
            .collect(Collectors.joining("|", SERVICE_ID_PREFIX, SERVICE_ID_SUFFIX));
    }

    /**
     * The redirect URIs a service id built by {@link #toServiceId(Collection)} matches. Any other service id, such as one
     * defined by hand or registered before, is returned as the only redirect URI, as it is.
     *
     * @param serviceId the service id
     * @return the redirect URIs
     */
    public static List<String> toRedirectUris(final String serviceId) {
        if (!Strings.CS.startsWith(serviceId, SERVICE_ID_PREFIX + QUOTE_START) || !Strings.CS.endsWith(serviceId, QUOTE_END + SERVICE_ID_SUFFIX)) {
            return CollectionUtils.wrapList(serviceId);
        }
        val body = serviceId.substring(SERVICE_ID_PREFIX.length(), serviceId.length() - SERVICE_ID_SUFFIX.length());
        val redirectUris = new ArrayList<String>();
        var current = new StringBuilder();
        var position = 0;
        while (position < body.length()) {
            if (body.charAt(position) == '|') {
                redirectUris.add(current.toString());
                current = new StringBuilder();
                position++;
                continue;
            }
            if (body.startsWith(ESCAPED_QUOTE_END, position)) {
                current.append(QUOTE_END);
                position += ESCAPED_QUOTE_END.length();
                continue;
            }
            val end = body.startsWith(QUOTE_START, position) ? body.indexOf(QUOTE_END, position + QUOTE_START.length()) : -1;
            if (end < 0) {
                return CollectionUtils.wrapList(serviceId);
            }
            current.append(body, position + QUOTE_START.length(), end);
            position = end + QUOTE_END.length();
        }
        redirectUris.add(current.toString());
        return redirectUris;
    }

    /**
     * Gets client registration response.
     *
     * @param registeredService    the registered service
     * @param configurationContext the configuration context
     * @return the client registration response
     */
    public static OidcClientRegistrationResponse getClientRegistrationResponse(
        final OidcRegisteredService registeredService,
        final OidcConfigurationContext configurationContext) {
        val clientResponse = new OidcClientRegistrationResponse();
        clientResponse.setApplicationType(registeredService.getApplicationType());
        clientResponse.setClientId(registeredService.getClientId());

        val decodedSecret = registeredService
            .getClientSecrets()
            .stream()
            .map(secret -> {
                val decoded = configurationContext.getRegisteredServiceCipherExecutor().decode(secret.getValue());
                return new OAuthRegisteredServiceClientSecret(Objects.requireNonNull(decoded), secret.getExpiration());
            })
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No client secret is defined for service " + registeredService.getName()));

        clientResponse.setClientSecret(decodedSecret.getValue());
        clientResponse.setSubjectType(registeredService.getSubjectType());
        clientResponse.setTokenEndpointAuthMethod(registeredService.getTokenEndpointAuthenticationMethod());
        clientResponse.setDpopBoundAccessTokens(registeredService.isDpopBoundAccessTokens() ? Boolean.TRUE : null);
        clientResponse.setTlsClientCertificateBoundAccessTokens(registeredService.isTlsClientCertificateBoundAccessTokens() ? Boolean.TRUE : null);
        clientResponse.setClientName(registeredService.getName());
        clientResponse.setRedirectUris(toRedirectUris(registeredService.getServiceId()));
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

        val validator = configurationContext.getUrlValidator();
        val keystore = OidcJsonWebKeyStoreUtils.resolveJsonWebKeySet(registeredService);
        FunctionUtils.doUnchecked(param -> {
            if (Objects.requireNonNull(validator).isValid(keystore)) {
                clientResponse.setJwksUri(keystore);
            } else if (ResourceUtils.doesResourceExist(keystore)) {
                val res = ResourceUtils.getResourceFrom(keystore);
                val jsonWebKeys = OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(res);
                clientResponse.setJwks(jsonWebKeys.toJson());
            } else if (StringUtils.isNotBlank(keystore)) {
                val jwks = new JsonWebKeySet(keystore);
                clientResponse.setJwks(jwks.toJson());
            }
            clientResponse.setLogo(registeredService.getLogo());
            clientResponse.setPolicyUri(registeredService.getInformationUrl());
            clientResponse.setTermsOfUseUri(registeredService.getPrivacyUrl());
            clientResponse.setRedirectUris(toRedirectUris(registeredService.getServiceId()));

            val serverPrefix = configurationContext.getCasProperties().getServer().getPrefix();
            val clientConfigUri = getClientConfigurationUri(registeredService, serverPrefix);
            clientResponse.setRegistrationClientUri(clientConfigUri);
        });
        if (!decodedSecret.isWithoutExpiration()) {
            clientResponse.setClientSecretExpiresAt(decodedSecret.toEffectiveExpiration().toEpochSecond());
        }
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
                                                   final String serverPrefix)
            throws URISyntaxException {
        return new URIBuilder(serverPrefix
            .concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_CONFIGURATION_URL))
            .addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .build()
            .toString();
    }
}
