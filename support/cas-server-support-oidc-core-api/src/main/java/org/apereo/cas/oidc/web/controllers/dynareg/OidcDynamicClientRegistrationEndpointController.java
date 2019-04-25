package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcDynamicClientRegistrationEndpointController extends BaseOAuth20Controller {
    private static final int GENERATED_CLIENT_NAME_LENGTH = 8;

    public OidcDynamicClientRegistrationEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     * @return the model and view
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcClientRegistrationResponse> handleRequestInternal(@RequestBody final String jsonInput,
                                                                                final HttpServletRequest request,
                                                                                final HttpServletResponse response) {
        try {
            val registrationRequest = (OidcClientRegistrationRequest) getOAuthConfigurationContext()
                .getClientRegistrationRequestSerializer().from(jsonInput);
            LOGGER.debug("Received client registration request [{}]", registrationRequest);

            val registeredService = new OidcRegisteredService();

            if (StringUtils.isNotBlank(registrationRequest.getClientName())) {
                registeredService.setName(registrationRequest.getClientName());
            } else {
                registeredService.setName(RandomStringUtils.randomAlphabetic(GENERATED_CLIENT_NAME_LENGTH));
            }
            registeredService.setSectorIdentifierUri(registrationRequest.getSectorIdentifierUri());
            registeredService.setSubjectType(registrationRequest.getSubjectType());
            if (StringUtils.equalsIgnoreCase(OidcSubjectTypes.PAIRWISE.getType(), registeredService.getSubjectType())) {
                registeredService.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());
            }

            if (StringUtils.isNotBlank(registrationRequest.getJwksUri())) {
                registeredService.setJwks(registrationRequest.getJwksUri());
                registeredService.setSignIdToken(true);
            }
            val uri = registrationRequest.getRedirectUris().stream().findFirst().get();
            registeredService.setServiceId(uri);

            registeredService.setClientId(getOAuthConfigurationContext().getClientIdGenerator().getNewString());
            registeredService.setClientSecret(getOAuthConfigurationContext().getClientSecretGenerator().getNewString());
            registeredService.setEvaluationOrder(Ordered.HIGHEST_PRECEDENCE);
            registeredService.setLogoutUrl(org.springframework.util.StringUtils.collectionToCommaDelimitedString(registrationRequest.getPostLogoutRedirectUris()));

            if (StringUtils.isNotBlank(registeredService.getLogo())) {
                registeredService.setLogo(registeredService.getLogo());
            }
            
            val supportedScopes = new HashSet<String>(getOAuthConfigurationContext().getCasProperties().getAuthn().getOidc().getScopes());
            val clientResponse = getClientRegistrationResponse(registrationRequest, registeredService);
            registeredService.setScopes(supportedScopes);
            val processedScopes = new LinkedHashSet<String>(supportedScopes);
            registeredService.setScopes(processedScopes);

            if (!registrationRequest.getDefaultAcrValues().isEmpty()) {
                val multifactorPolicy = new DefaultRegisteredServiceMultifactorPolicy();
                multifactorPolicy.setMultifactorAuthenticationProviders(new HashSet<>(registrationRequest.getDefaultAcrValues()));
                registeredService.setMultifactorPolicy(multifactorPolicy);
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

            registeredService.setDescription("Dynamically registered service "
                .concat(registeredService.getName())
                .concat(" with grant types ")
                .concat(String.join(",", clientResponse.getGrantTypes()))
                .concat(" and with scopes ")
                .concat(String.join(",", registeredService.getScopes()))
                .concat(" and response types ")
                .concat(String.join(",", clientResponse.getResponseTypes())));
            registeredService.setDynamicallyRegistered(true);
            getOAuthConfigurationContext().getProfileScopeToAttributesFilter().reconcile(registeredService);

            return new ResponseEntity<>(clientResponse, HttpStatus.CREATED);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            val map = new HashMap<String, String>();
            map.put("error", "invalid_client_metadata");
            map.put("error_description", StringUtils.defaultString(e.getMessage(), "None"));
            return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Gets client registration response.
     *
     * @param registrationRequest the registration request
     * @param registeredService   the registered service
     * @return the client registration response
     */
    protected OidcClientRegistrationResponse getClientRegistrationResponse(final OidcClientRegistrationRequest registrationRequest,
                                                                           final OidcRegisteredService registeredService) {
        val clientResponse = new OidcClientRegistrationResponse();
        clientResponse.setApplicationType(registrationRequest.getApplicationType());
        clientResponse.setClientId(registeredService.getClientId());
        clientResponse.setClientSecret(registeredService.getClientSecret());
        clientResponse.setSubjectType(registrationRequest.getSubjectType());
        clientResponse.setTokenEndpointAuthMethod(registrationRequest.getTokenEndpointAuthMethod());
        clientResponse.setClientName(registeredService.getName());
        clientResponse.setGrantTypes(CollectionUtils.wrapList(OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(),
            OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase()));
        clientResponse.setRedirectUris(CollectionUtils.wrap(registeredService.getServiceId()));
        clientResponse.setResponseTypes(CollectionUtils.wrap(OAuth20ResponseTypes.CODE.name().toLowerCase()));
        return clientResponse;
    }
}
