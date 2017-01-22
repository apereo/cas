package org.apereo.cas.web.controllers;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.OidcClientRegistrationRequest;
import org.apereo.cas.OidcClientRegistrationResponse;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthGrantType;
import org.apereo.cas.support.oauth.OAuthResponseType;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.BaseOAuthWrapperController;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcDynamicClientRegistrationEndpointController extends BaseOAuthWrapperController {
    private StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer;
    private RandomStringGenerator clientIdGenerator;
    private RandomStringGenerator clientSecretGenerator;

    public OidcDynamicClientRegistrationEndpointController(final ServicesManager servicesManager,
                                                           final TicketRegistry ticketRegistry,
                                                           final OAuth20Validator validator,
                                                           final AccessTokenFactory accessTokenFactory,
                                                           final PrincipalFactory principalFactory,
                                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                           final StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer,
                                                           final RandomStringGenerator clientIdGenerator,
                                                           final RandomStringGenerator clientSecretGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory, principalFactory, webApplicationServiceServiceFactory);
        this.clientRegistrationRequestSerializer = clientRegistrationRequestSerializer;
        this.clientIdGenerator = clientIdGenerator;
        this.clientSecretGenerator = clientSecretGenerator;
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
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcClientRegistrationResponse> handleRequestInternal(@RequestBody final String jsonInput,
                                                                                final HttpServletRequest request,
                                                                                final HttpServletResponse response) throws Exception {
        try {
            final OidcClientRegistrationRequest registrationRequest = this.clientRegistrationRequestSerializer.from(jsonInput);
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

            final OidcClientRegistrationResponse clientResponse = getClientRegistrationResponse(registrationRequest, registeredService);
            registeredService.setDescription("Dynamically registered service "
                    .concat(registeredService.getName())
                    .concat(" with grant types ")
                    .concat(clientResponse.getGrantTypes().toString())
                    .concat(" and response types ")
                    .concat(clientResponse.getResponseTypes().toString()));
            getServicesManager().save(registeredService);
            return new ResponseEntity<>(clientResponse, HttpStatus.CREATED);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            final Map<String, String> map = new HashMap<>();
            map.put("error", "invalid_client_metadata");
            map.put("error_message", e.getMessage());
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
        final OidcClientRegistrationResponse clientResponse = new OidcClientRegistrationResponse();
        clientResponse.setApplicationType("web");
        clientResponse.setClientId(registeredService.getClientId());
        clientResponse.setClientSecret(registeredService.getClientSecret());
        clientResponse.setSubjectType("public");
        clientResponse.setTokenEndpointAuthMethod(registrationRequest.getTokenEndpointAuthMethod());
        clientResponse.setClientName(registeredService.getName());
        clientResponse.setGrantTypes(Arrays.asList(OAuthGrantType.AUTHORIZATION_CODE.name().toLowerCase(),
                OAuthGrantType.REFRESH_TOKEN.name().toLowerCase()));
        clientResponse.setRedirectUris(Collections.singletonList(registeredService.getServiceId()));
        clientResponse.setResponseTypes(Collections.singletonList(OAuthResponseType.CODE.name().toLowerCase()));
        return clientResponse;
    }
}
