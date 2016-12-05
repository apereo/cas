package org.apereo.cas.web.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ClientRegistrationRequest;
import org.apereo.cas.ClientRegistrationResponse;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.apereo.cas.support.oauth.web.OAuthGrantType;
import org.apereo.cas.support.oauth.web.OAuthResponseType;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcDynamicClientRegistrationEndpointController extends BaseOAuthWrapperController {
    private StringSerializer<ClientRegistrationRequest> clientRegistrationRequestSerializer;
    private RandomStringGenerator clientIdGenerator;
    private RandomStringGenerator clientSecretGenerator;

    public void setClientIdGenerator(final RandomStringGenerator clientIdGenerator) {
        this.clientIdGenerator = clientIdGenerator;
    }

    public void setClientSecretGenerator(final RandomStringGenerator clientSecretGenerator) {
        this.clientSecretGenerator = clientSecretGenerator;
    }

    public void setClientRegistrationRequestSerializer(final StringSerializer<ClientRegistrationRequest> clientRegistrationRequestSerializer) {
        this.clientRegistrationRequestSerializer = clientRegistrationRequestSerializer;
    }

    /**
     * Handle request.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClientRegistrationResponse> handleRequestInternal(@RequestBody final String jsonInput,
                                                                            final HttpServletRequest request,
                                                                            final HttpServletResponse response) throws Exception {
        try {
            final ClientRegistrationRequest registrationRequest = this.clientRegistrationRequestSerializer.from(jsonInput);
            logger.debug("Received client registration request {}", registrationRequest);

            final OidcRegisteredService registeredService = new OidcRegisteredService();
            registeredService.setName(registrationRequest.getClientName());
            registeredService.setGenerateRefreshToken(true);

            if (StringUtils.isNotBlank(registrationRequest.getJwksUri())) {
                registeredService.setJwks(registrationRequest.getJwksUri());
                registeredService.setSignIdToken(true);
            }
            final String uri = registrationRequest.getRedirectUris().stream().findFirst().get();
            registeredService.setServiceId(uri);

            registeredService.setClientId(clientIdGenerator.getNewString());
            registeredService.setClientSecret(clientSecretGenerator.getNewString());
            registeredService.setEvaluationOrder(Integer.MIN_VALUE);

            final ClientRegistrationResponse clientResponse = getClientRegistrationResponse(registrationRequest, registeredService);
            registeredService.setDescription("Dynamically registered service "
                    .concat(registeredService.getName())
                    .concat(" with grant types ")
                    .concat(clientResponse.getGrantTypes().toString())
                    .concat(" and response types ")
                    .concat(clientResponse.getResponseTypes().toString()));
            this.servicesManager.save(registeredService);
            return new ResponseEntity(clientResponse, HttpStatus.CREATED);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            final Map<String, String> map = ImmutableMap.of("error", "invalid_client_metadata",
                    "error_message", e.getMessage());
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
    protected ClientRegistrationResponse getClientRegistrationResponse(final ClientRegistrationRequest registrationRequest,
                                                                       final OidcRegisteredService registeredService) {
        final ClientRegistrationResponse clientResponse = new ClientRegistrationResponse();
        clientResponse.setApplicationType("web");
        clientResponse.setClientId(registeredService.getClientId());
        clientResponse.setClientSecret(registeredService.getClientSecret());
        clientResponse.setSubjectType("public");
        clientResponse.setTokenEndpointAuthMethod(registrationRequest.getTokenEndpointAuthMethod());
        clientResponse.setClientName(registeredService.getName());
        clientResponse.setGrantTypes(Lists.newArrayList(OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase(),
                OAuthGrantType.REFRESH_TOKEN.name().toLowerCase()));
        clientResponse.setRedirectUris(Lists.newArrayList(registeredService.getServiceId()));
        clientResponse.setResponseTypes(Lists.newArrayList(OAuthResponseType.CODE.name().toLowerCase()));
        return clientResponse;
    }
}
