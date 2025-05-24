package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link OidcClientConfigurationEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcClientConfigurationEndpointController extends BaseOidcController {
    public OidcClientConfigurationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle request response entity.
     *
     * @param clientId the client id
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_CONFIGURATION_URL,
        "/**/" + OidcConstants.CLIENT_CONFIGURATION_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle client configuration request", parameters = @Parameter(name = OAuth20Constants.CLIENT_ID, description = "Client ID", required = true))
    public ResponseEntity handleRequestInternal(
        @RequestParam(name = OAuth20Constants.CLIENT_ID)
        final String clientId,
        final HttpServletRequest request, final HttpServletResponse response) {

        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.CLIENT_CONFIGURATION_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
        if (service instanceof final OidcRegisteredService oidcRegisteredService) {
            val prefix = getConfigurationContext().getCasProperties().getServer().getPrefix();
            val regResponse = OidcClientRegistrationUtils.getClientRegistrationResponse(oidcRegisteredService, prefix);
            return new ResponseEntity<>(regResponse, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Handle updates response entity.
     *
     * @param clientId  the client id
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     * @return the response entity
     * @throws Exception the exception
     */
    @PatchMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CLIENT_CONFIGURATION_URL,
        "/**/" + OidcConstants.CLIENT_CONFIGURATION_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle client configuration updates",
        parameters = @Parameter(name = OAuth20Constants.CLIENT_ID, in = ParameterIn.QUERY, description = "Client ID", required = true),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Client registration request",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = OidcClientRegistrationRequest.class)
            )
        )
    )
    public ResponseEntity handleUpdates(
        @RequestParam(name = OAuth20Constants.CLIENT_ID)
        final String clientId,
        @RequestBody(required = false)
        final String jsonInput,
        final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.CLIENT_CONFIGURATION_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        var service = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), clientId, OidcRegisteredService.class);
        if (service != null) {
            if (StringUtils.isNotBlank(jsonInput)) {
                val registrationRequest = (OidcClientRegistrationRequest) getConfigurationContext()
                    .getClientRegistrationRequestSerializer().from(jsonInput);
                LOGGER.debug("Received client registration request [{}]", registrationRequest);
                service = getConfigurationContext().getClientRegistrationRequestTranslator()
                    .translate(registrationRequest, Optional.of(service));
            }
            val clientSecretExp = Beans.newDuration(getConfigurationContext().getCasProperties()
                .getAuthn().getOidc().getRegistration().getClientSecretExpiration()).toSeconds();
            if (clientSecretExp > 0 && getConfigurationContext().getClientSecretValidator().isClientSecretExpired(service)) {
                val currentTime = ZonedDateTime.now(ZoneOffset.UTC);
                val expirationDate = currentTime.plusSeconds(clientSecretExp);
                service.setClientSecretExpiration(expirationDate.toEpochSecond());
                service.setClientSecret(getConfigurationContext().getClientSecretGenerator().getNewString());
                LOGGER.debug("Client secret shall expire at [{}] while now is [{}]", expirationDate, currentTime);
            }
            
            val clientResponse = OidcClientRegistrationUtils.getClientRegistrationResponse(service,
                getConfigurationContext().getCasProperties().getServer().getPrefix());
            return new ResponseEntity<>(clientResponse, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().build();
    }
}
