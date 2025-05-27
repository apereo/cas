package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcDynamicClientRegistrationEndpointController extends BaseOidcController {
    public OidcDynamicClientRegistrationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle request.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     * @return the model and view
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL,
        "/**/" + OidcConstants.REGISTRATION_URL
    }, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC dynamic client registration request",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Client registration request",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = OidcClientRegistrationRequest.class)
            )
        ))
    public ResponseEntity handleRequestInternal(
        @RequestBody
        final String jsonInput,
        final HttpServletRequest request,
        final HttpServletResponse response) {

        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.REGISTRATION_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity(body, HttpStatus.BAD_REQUEST);
        }
        try {
            val registrationRequest = (OidcClientRegistrationRequest) getConfigurationContext()
                .getClientRegistrationRequestSerializer().from(jsonInput);
            LOGGER.debug("Received client registration request [{}]", registrationRequest);
            val registeredService = getConfigurationContext().getClientRegistrationRequestTranslator()
                .translate(registrationRequest, Optional.empty());
            registeredService.markAsDynamicallyRegistered();

            val savedService = (OidcRegisteredService) getConfigurationContext().getServicesManager().save(registeredService);
            val clientResponse = OidcClientRegistrationUtils.getClientRegistrationResponse(savedService,
                getConfigurationContext().getCasProperties().getServer().getPrefix());
            val accessToken = generateRegistrationAccessToken(request, response, savedService, registrationRequest);

            val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(getConfigurationContext(),
                savedService, accessToken, accessToken.getService(), false);
            val encodedAccessToken = cipher.encode(accessToken.getId());
            clientResponse.setRegistrationAccessToken(encodedAccessToken);
            return new ResponseEntity<>(clientResponse, HttpStatus.CREATED);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            val map = OAuth20Utils.getErrorResponseBody("invalid_client_metadata",
                StringUtils.defaultIfBlank(e.getMessage(), "None"));
            return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
        }
    }

    protected OAuth20AccessToken generateRegistrationAccessToken(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final OidcRegisteredService registeredService,
        final OidcClientRegistrationRequest registrationRequest) throws Throwable {

        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(registeredService.getClientId());
        val authn = DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
        val clientConfigUri = OidcClientRegistrationUtils.getClientConfigurationUri(registeredService,
            getConfigurationContext().getCasProperties().getServer().getPrefix());
        val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(clientConfigUri);
        val factory = (OAuth20AccessTokenFactory) getConfigurationContext().getTicketFactory().get(OAuth20AccessToken.class);
        val accessToken = factory.create(service, authn,
            List.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE),
            registeredService.getClientId(),
            OAuth20ResponseTypes.NONE, OAuth20GrantTypes.NONE);
        getConfigurationContext().getTicketRegistry().addTicket(accessToken);
        return accessToken;
    }
}
