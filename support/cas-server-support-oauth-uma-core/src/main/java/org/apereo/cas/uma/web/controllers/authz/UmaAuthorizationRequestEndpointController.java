package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.idtoken.IdTokenGenerationContext;
import org.apereo.cas.uma.UmaConfigurationContext;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionResult;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link UmaAuthorizationRequestEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Controller("umaAuthorizationRequestEndpointController")
@Tag(name = "User Managed Access")
public class UmaAuthorizationRequestEndpointController extends BaseUmaEndpointController {

    public UmaAuthorizationRequestEndpointController(final UmaConfigurationContext umaConfigurationContext) {
        super(umaConfigurationContext);
    }

    /**
     * Handle authorization request.
     *
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL)
    @Operation(
        summary = "Handle UMA authorization request",
        description = "Handles the UMA authorization request and returns a response"
    )
    public ResponseEntity handleAuthorizationRequest(
        @RequestBody
        final String body,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response, OAuth20Constants.UMA_AUTHORIZATION_SCOPE);
            val umaRequest = MAPPER.readValue(JsonValue.readHjson(body).toString(), UmaAuthorizationRequest.class);

            if (StringUtils.isBlank(umaRequest.getGrantType())) {
                return new ResponseEntity("Unable to accept authorization request; grant type is missing", HttpStatus.BAD_REQUEST);
            }
            if (!umaRequest.getGrantType().equalsIgnoreCase(OAuth20GrantTypes.UMA_TICKET.getType())) {
                return new ResponseEntity("Unable to accept authorization request; need grant type "
                                          + OAuth20GrantTypes.UMA_TICKET.getType(), HttpStatus.BAD_REQUEST);
            }

            if (StringUtils.isBlank(umaRequest.getTicket())) {
                return new ResponseEntity("Unable to accept authorization request; ticket parameter is missing", HttpStatus.BAD_REQUEST);
            }
            val permissionTicket = getUmaConfigurationContext().getTicketRegistry()
                .getTicket(umaRequest.getTicket(), UmaPermissionTicket.class);
            val resourceSet = permissionTicket.getResourceSet();
            if (resourceSet == null || resourceSet.getPolicies() == null || resourceSet.getPolicies().isEmpty()) {
                return new ResponseEntity("resource-set or linked policies are undefined", HttpStatus.BAD_REQUEST);
            }

            val results = getUmaConfigurationContext().getClaimPermissionExaminer().examine(permissionTicket);
            if (results.isSatisfied()) {
                return generateRequestingPartyToken(request, response, profileResult, umaRequest, permissionTicket);
            }

            return handleMismatchedClaims(request, response, profileResult, results, permissionTicket);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ResponseEntity("Unable to handle authorization request", HttpStatus.BAD_REQUEST);
    }

    protected ResponseEntity handleMismatchedClaims(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final UserProfile profileResult,
        final UmaResourceSetClaimPermissionResult analysisResult,
        final UmaPermissionTicket permissionTicket) {

        val model = new LinkedHashMap<String, Object>();
        model.put(OAuth20Constants.ERROR, OAuth20Constants.NEED_INFO);

        val claims = new UmaAuthorizationNeedInfoResponse();
        claims.setRedirectUser(true);
        claims.setTicket(permissionTicket.getId());

        val requiredClaims = analysisResult.getDetails()
            .values()
            .stream()
            .map(err -> err.getUnmatchedClaims().keySet().stream().map(String::toString).collect(Collectors.toSet()))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        claims.setRequiredClaims(requiredClaims);

        val requiredScopes = analysisResult.getDetails()
            .values()
            .stream()
            .map(err -> err.getUnmatchedScopes().stream().map(String::toString).collect(Collectors.toSet()))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        claims.setRequiredScopes(requiredScopes);

        val details = CollectionUtils.wrap(OAuth20Constants.REQUESTING_PARTY_CLAIMS, claims);
        model.put(OAuth20Constants.ERROR_DETAILS, details);

        return new ResponseEntity(model, HttpStatus.PERMANENT_REDIRECT);
    }

    protected ResponseEntity generateRequestingPartyToken(
        final HttpServletRequest request, final HttpServletResponse response,
        final UserProfile profileResult, final UmaAuthorizationRequest umaRequest,
        final UmaPermissionTicket permissionTicket) throws Throwable {
        val currentAat = (OAuth20AccessToken) profileResult.getAttribute(OAuth20AccessToken.class.getName());
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getUmaConfigurationContext().getServicesManager(),
            OAuth20Utils.getClientIdFromAuthenticatedProfile(profileResult));

        val scopes = new LinkedHashSet<>(permissionTicket.getScopes());
        scopes.add(OAuth20Constants.UMA_AUTHORIZATION_SCOPE);
        scopes.addAll(permissionTicket.getResourceSet().getScopes());
        
        val tokenRequestContext = AccessTokenRequestContext
            .builder()
            .authentication(currentAat.getAuthentication())
            .ticketGrantingTicket(currentAat.getTicketGrantingTicket())
            .grantType(OAuth20GrantTypes.UMA_TICKET)
            .responseType(OAuth20ResponseTypes.NONE)
            .registeredService(registeredService)
            .generateRefreshToken(false)
            .scopes(scopes)
            .service(currentAat.getService())
            .build();

        val result = getUmaConfigurationContext().getAccessTokenGenerator().generate(tokenRequestContext);

        val givenAccessToken = result.getAccessToken().orElseThrow();
        val accessToken = resolveAccessToken(givenAccessToken);

        val cipher = OAuth20JwtAccessTokenEncoder.toEncodableCipher(getUmaConfigurationContext(),
            registeredService, accessToken, accessToken.getService(), false);
        val encodedAccessToken = cipher.encode(accessToken.getId());
        val userProfile = OAuth20Utils.getAuthenticatedUserProfile(new JEEContext(request, response),
            getUmaConfigurationContext().getSessionStore());
        userProfile.addAttribute(UmaPermissionTicket.class.getName(), permissionTicket);
        userProfile.addAttribute(ResourceSet.class.getName(), permissionTicket.getResourceSet());

        val idTokenContext = IdTokenGenerationContext.builder()
            .accessToken(accessToken)
            .userProfile(userProfile)
            .responseType(OAuth20ResponseTypes.CODE)
            .grantType(OAuth20GrantTypes.UMA_TICKET)
            .registeredService(registeredService)
            .build();
        val idToken = getUmaConfigurationContext().getRequestingPartyTokenGenerator().generate(idTokenContext);
        accessToken.setIdToken(idToken.token());
        if (!accessToken.isStateless()) {
            getUmaConfigurationContext().getTicketRegistry().updateTicket(accessToken);
            if (StringUtils.isNotBlank(umaRequest.getRpt())) {
                getUmaConfigurationContext().getTicketRegistry().deleteTicket(umaRequest.getRpt());
            }
        }
        val model = CollectionUtils.wrap("rpt", encodedAccessToken, "code", HttpStatus.CREATED);
        return new ResponseEntity<>(model, HttpStatus.OK);
    }
}
