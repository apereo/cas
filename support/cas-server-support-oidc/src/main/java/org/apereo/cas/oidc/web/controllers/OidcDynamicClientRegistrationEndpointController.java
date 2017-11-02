package org.apereo.cas.oidc.web.controllers;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.OidcSubjectTypes;
import org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcDynamicClientRegistrationEndpointController extends BaseOAuth20Controller {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcDynamicClientRegistrationEndpointController.class);

    private final StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer;
    private final RandomStringGenerator clientIdGenerator;
    private final RandomStringGenerator clientSecretGenerator;

    public OidcDynamicClientRegistrationEndpointController(final ServicesManager servicesManager,
                                                           final TicketRegistry ticketRegistry,
                                                           final OAuth20Validator validator,
                                                           final AccessTokenFactory accessTokenFactory,
                                                           final PrincipalFactory principalFactory,
                                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                           final StringSerializer<OidcClientRegistrationRequest> clientRegistrationRequestSerializer,
                                                           final RandomStringGenerator clientIdGenerator,
                                                           final RandomStringGenerator clientSecretGenerator,
                                                           final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                           final CasConfigurationProperties casProperties,
                                                           final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        super(servicesManager, ticketRegistry, validator, accessTokenFactory,
                principalFactory, webApplicationServiceServiceFactory,
                scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
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
     */
    @PostMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OidcClientRegistrationResponse> handleRequestInternal(@RequestBody final String jsonInput,
                                                                                final HttpServletRequest request,
                                                                                final HttpServletResponse response) {
        try {
            final OidcClientRegistrationRequest registrationRequest = this.clientRegistrationRequestSerializer.from(jsonInput);
            LOGGER.debug("Received client registration request [{}]", registrationRequest);

            if (registrationRequest.getScopes().isEmpty()) {
                throw new Exception("Registration request does not contain any scope values");
            }
            if (!registrationRequest.getScope().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
                throw new Exception("Registration request scopes do not contain " + OidcConstants.StandardScopes.OPENID.getScope());
            }

            final OidcRegisteredService registeredService = new OidcRegisteredService();
            registeredService.setName(registrationRequest.getClientName());
            
            registeredService.setSectorIdentifierUri(registrationRequest.getSectorIdentifierUri());
            registeredService.setSubjectType(registrationRequest.getSubjectType());
            if (StringUtils.equalsIgnoreCase(OidcSubjectTypes.PAIRWISE.getType(), registeredService.getSubjectType())) {
                registeredService.setUsernameAttributeProvider(new PairwiseOidcRegisteredServiceUsernameAttributeProvider());    
            }
            
            if (StringUtils.isNotBlank(registrationRequest.getJwksUri())) {
                registeredService.setJwks(registrationRequest.getJwksUri());
                registeredService.setSignIdToken(true);
            }
            final String uri = registrationRequest.getRedirectUris().stream().findFirst().get();
            registeredService.setServiceId(uri);

            registeredService.setClientId(clientIdGenerator.getNewString());
            registeredService.setClientSecret(clientSecretGenerator.getNewString());
            registeredService.setEvaluationOrder(Integer.MIN_VALUE);

            final Set<String> supportedScopes = new HashSet<>(casProperties.getAuthn().getOidc().getScopes());
            supportedScopes.retainAll(registrationRequest.getScopes());

            final OidcClientRegistrationResponse clientResponse = getClientRegistrationResponse(registrationRequest, registeredService);
            registeredService.setScopes(supportedScopes);
            final Set<String> processedScopes = new LinkedHashSet<>(supportedScopes);
            registeredService.setScopes(processedScopes);
            registeredService.setDescription("Dynamically registered service "
                    .concat(registeredService.getName())
                    .concat(" with grant types ")
                    .concat(clientResponse.getGrantTypes().stream().collect(Collectors.joining(",")))
                    .concat(" and with scopes ")
                    .concat(registeredService.getScopes().stream().collect(Collectors.joining(",")))
                    .concat(" and response types ")
                    .concat(clientResponse.getResponseTypes().stream().collect(Collectors.joining(","))));
            registeredService.setDynamicallyRegistered(true);
            scopeToAttributesFilter.reconcile(registeredService);

            return new ResponseEntity<>(clientResponse, HttpStatus.CREATED);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        clientResponse.setGrantTypes(CollectionUtils.wrapList(OAuth20GrantTypes.AUTHORIZATION_CODE.name().toLowerCase(),
                OAuth20GrantTypes.REFRESH_TOKEN.name().toLowerCase()));
        clientResponse.setRedirectUris(CollectionUtils.wrap(registeredService.getServiceId()));
        clientResponse.setResponseTypes(CollectionUtils.wrap(OAuth20ResponseTypes.CODE.name().toLowerCase()));
        return clientResponse;
    }
}
