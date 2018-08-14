package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.IdTokenGeneratorService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.uma.claim.UmaResourceSetClaimPermissionExaminer;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicket;
import org.apereo.cas.uma.ticket.permission.UmaPermissionTicketFactory;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.uma.ticket.resource.ResourceSetPolicyPermission;
import org.apereo.cas.uma.ticket.resource.repository.ResourceSetRepository;
import org.apereo.cas.uma.web.controllers.BaseUmaEndpointController;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonObject;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link UmaAuthorizationRequestEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Controller("umaAuthorizationRequestEndpointController")
public class UmaAuthorizationRequestEndpointController extends BaseUmaEndpointController {
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final OAuth20TokenGenerator accessTokenGenerator;
    private final UmaResourceSetClaimPermissionExaminer claimPermissionExaminer;
    private final IdTokenGeneratorService requestingPartyTokenGenerator;

    public UmaAuthorizationRequestEndpointController(final UmaPermissionTicketFactory umaPermissionTicketFactory,
                                                     final ResourceSetRepository umaResourceSetRepository,
                                                     final CasConfigurationProperties casProperties,
                                                     final ServicesManager servicesManager,
                                                     final TicketRegistry ticketRegistry,
                                                     final OAuth20TokenGenerator accessTokenGenerator,
                                                     final UmaResourceSetClaimPermissionExaminer claimPermissionExaminer,
                                                     final IdTokenGeneratorService requestingPartyTokenGenerator) {
        super(umaPermissionTicketFactory, umaResourceSetRepository, casProperties);
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.accessTokenGenerator = accessTokenGenerator;
        this.claimPermissionExaminer = claimPermissionExaminer;
        this.requestingPartyTokenGenerator = requestingPartyTokenGenerator;
    }

    /**
     * Handle authorization request.
     *
     * @param body     the body
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(value = '/' + OAuth20Constants.BASE_OAUTH20_URL + "/" + OAuth20Constants.UMA_AUTHORIZATION_REQUEST_URL,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleAuthorizationRequest(@RequestBody final String body,
                                                     final HttpServletRequest request, final HttpServletResponse response) {
        try {
            val profileResult = getAuthenticatedProfile(request, response, OAuth20Constants.UMA_AUTHORIZATION_SCOPE);
            val umaRequest = MAPPER.readValue(body, UmaAuthorizationRequest.class);

            if (!StringUtils.isBlank(umaRequest.getGrantType())) {
                return new ResponseEntity("Unable to accept authorization request; grant type is missing", HttpStatus.BAD_REQUEST);
            }
            if (!umaRequest.getGrantType().equalsIgnoreCase(OAuth20GrantTypes.UMA_TICKET.getType())) {
                return new ResponseEntity("Unable to accept authorization request; required grant type "
                    + OAuth20GrantTypes.UMA_TICKET.getType(), HttpStatus.BAD_REQUEST);
            }

            if (StringUtils.isBlank(umaRequest.getTicket())) {
                return new ResponseEntity("Unable to accept authorization request; ticket parameter is missing", HttpStatus.BAD_REQUEST);
            }
            val permissionTicket = this.ticketRegistry.getTicket(umaRequest.getTicket(), UmaPermissionTicket.class);
            if (permissionTicket == null || permissionTicket.isExpired()) {
                return new ResponseEntity("Permission ticket is invalid or has expired", HttpStatus.BAD_REQUEST);
            }
            val resourceSet = permissionTicket.getResourceSet();
            if (resourceSet == null || resourceSet.getPolicies() == null || resourceSet.getPolicies().isEmpty()) {
                return new ResponseEntity("resource-set or linked policies are undefined", HttpStatus.BAD_REQUEST);
            }

            val results = claimPermissionExaminer.examine(resourceSet, permissionTicket);
            if (results.isEmpty()) {
                return generateRequestingPartyToken(request, response, profileResult, umaRequest, permissionTicket, resourceSet);
            }

            return handleMismatchedClaims(request, response, resourceSet, profileResult, results, permissionTicket);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ResponseEntity("Unable to handle authorization request", HttpStatus.BAD_REQUEST);
    }

    @SneakyThrows
    private ResponseEntity handleMismatchedClaims(final HttpServletRequest request,
                                                  final HttpServletResponse response,
                                                  final ResourceSet resourceSet,
                                                  final CommonProfile profileResult,
                                                  final Map<ResourceSetPolicyPermission, Collection<String>> unmatchedResults,
                                                  final UmaPermissionTicket permissionTicket) {

        val model = new LinkedHashMap<>();
        model.put(OAuth20Constants.ERROR, OAuth20Constants.NEED_INFO);

        val claims = new MismatchedClaimsResponse();
        claims.setRedirectUser(true);
        claims.setTicket(permissionTicket.getId());

        val requiredClaims = unmatchedResults.entrySet()
            .stream()
            .map(Map.Entry::getValue)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        claims.setRequiredClaims(requiredClaims);

        val details = new JsonObject();
        details.add(OAuth20Constants.REQUESTING_PARTY_CLAIMS, MAPPER.writeValueAsString(claims));

        model.put(OAuth20Constants.ERROR_DETAILS, details);

        return new ResponseEntity(model, HttpStatus.PERMANENT_REDIRECT);
    }

    private ResponseEntity generateRequestingPartyToken(final HttpServletRequest request, final HttpServletResponse response,
                                                        final CommonProfile profileResult, final UmaAuthorizationRequest umaRequest,
                                                        final UmaPermissionTicket permissionTicket, final ResourceSet resourceSet) {
        val currentAat = profileResult.getAttribute(AccessToken.class.getName(), AccessToken.class);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager,
            OAuth20Utils.getClientIdFromAuthenticatedProfile(profileResult));

        val scopes = new LinkedHashSet<>();
        scopes.add(OAuth20Constants.UMA_AUTHORIZATION_SCOPE);
        scopes.addAll(permissionTicket.getScopes());
        scopes.addAll(resourceSet.getScopes());

        val holder = AccessTokenRequestDataHolder.builder()
            .authentication(currentAat.getAuthentication())
            .ticketGrantingTicket(currentAat.getTicketGrantingTicket())
            .grantType(OAuth20GrantTypes.UMA_TICKET)
            .responseType(OAuth20ResponseTypes.NONE)
            .registeredService(registeredService)
            .generateRefreshToken(false)
            .scopes(CollectionUtils.wrapSet())
            .build();

        val result = accessTokenGenerator.generate(holder);
        if (!result.getAccessToken().isPresent()) {
            return new ResponseEntity("Unable to generate access token", HttpStatus.BAD_REQUEST);
        }

        val accessToken = result.getAccessToken().get();
        val timeout = Beans.newDuration(casProperties.getAuthn().getUma().getRequestingPartyToken().getMaxTimeToLiveInSeconds()).getSeconds();
        val idToken = requestingPartyTokenGenerator.generate(request, response, accessToken,
            timeout, OAuth20ResponseTypes.CODE, registeredService);
        accessToken.setIdToken(idToken);
        this.ticketRegistry.updateTicket(accessToken);

        if (StringUtils.isNotBlank(umaRequest.getRpt())) {
            this.ticketRegistry.deleteTicket(umaRequest.getRpt());
        }
        val model = CollectionUtils.wrap("rpt", accessToken.getId(), "code", HttpStatus.CREATED);
        return new ResponseEntity(model, HttpStatus.OK);
    }

    /**
     * The mismatched claims response.
     */
    @Data
    public static class MismatchedClaimsResponse implements Serializable {
        private static final long serialVersionUID = -8719088128201373899L;

        @JsonProperty(value = "redirect_user", defaultValue = "true")
        private boolean redirectUser = true;

        @JsonProperty
        private String ticket;

        @JsonProperty("required_claims")
        private Collection<String> requiredClaims = new LinkedHashSet<>();
    }

}
