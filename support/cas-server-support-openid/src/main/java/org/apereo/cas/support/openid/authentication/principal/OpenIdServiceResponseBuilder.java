package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.validation.Assertion;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.openid4java.association.Association;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds responses to Openid authN requests.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 4.2
 */
@Slf4j
@Deprecated(since = "6.2.0")
public class OpenIdServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -4581238964007702423L;

    private final ServerManager serverManager;

    private final transient CentralAuthenticationService centralAuthenticationService;

    private final String openIdPrefixUrl;

    public OpenIdServiceResponseBuilder(final String openIdPrefixUrl, final ServerManager serverManager,
                                        final CentralAuthenticationService centralAuthenticationService,
                                        final ServicesManager servicesManager) {
        super(servicesManager);
        this.serverManager = serverManager;
        this.openIdPrefixUrl = openIdPrefixUrl;
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Generates an Openid response.
     * If no ticketId is found, response is negative.
     * If we have a ticket id, then we check if we have an association.
     * If so, we ask OpenId server manager to generate the answer according with the existing association.
     * If not, we send back an answer with the ticket id as association handle.
     * This will force the consumer to ask a verification, which will validate the service ticket.
     *
     * @param ticketId              the service ticket to provide to the service.
     * @param webApplicationService the service requesting an openid response
     * @return the generated authentication answer
     */
    @Override
    public Response build(final WebApplicationService webApplicationService, final String ticketId, final Authentication authentication) {

        val service = (OpenIdService) webApplicationService;
        val parameterList = new ParameterList(HttpRequestUtils.getHttpServletRequestFromRequestAttributes().getParameterMap());

        val parameters = new HashMap<String, String>();

        if (StringUtils.isBlank(ticketId)) {
            parameters.put(OpenIdProtocolConstants.OPENID_MODE, OpenIdProtocolConstants.CANCEL);
            return buildRedirect(service, parameters);
        }

        val association = getAssociation(serverManager, parameterList);
        val associated = association != null;
        val associationValid = isAssociationValid(association);
        var successFullAuthentication = true;

        var assertion = (Assertion) null;
        try {
            if (associated && associationValid) {
                assertion = centralAuthenticationService.validateServiceTicket(ticketId, service);
                LOGGER.debug("Validated openid ticket [{}] for [{}]", ticketId, service);
            } else if (!associated) {
                LOGGER.debug("Responding to non-associated mode. Service ticket [{}] must be validated by the RP", ticketId);
            } else {
                LOGGER.warn("Association does not exist or is not valid");
                successFullAuthentication = false;
            }
        } catch (final AbstractTicketException e) {
            LOGGER.error("Could not validate ticket : [{}]", e.getMessage(), e);
            successFullAuthentication = false;
        }
        val id = determineIdentity(service, assertion);
        return buildAuthenticationResponse(service, parameters, successFullAuthentication, id, parameterList);
    }

    /**
     * Determine identity.
     *
     * @param service   the service
     * @param assertion the assertion
     * @return the string
     */
    protected String determineIdentity(final OpenIdService service, final Assertion assertion) {
        if (assertion != null && OpenIdProtocolConstants.OPENID_IDENTIFIERSELECT.equals(service.getIdentity())) {
            return this.openIdPrefixUrl + '/' + assertion.getPrimaryAuthentication().getPrincipal().getId();
        }
        return service.getIdentity();
    }

    /**
     * We sign directly (final 'true') because we don't add extensions
     * response message can be either a DirectError or an AuthSuccess here.
     * Note:
     * The association handle returned in the Response is either the 'public'
     * created in a previous association, or is a 'private' handle created
     * specifically for the verification step when in non-association mode
     *
     * @param service                   the service
     * @param parameters                the parameters
     * @param successFullAuthentication the success full authentication
     * @param id                        the id
     * @param parameterList             the parameter list
     * @return response response
     */
    protected Response buildAuthenticationResponse(final OpenIdService service,
                                                   final Map<String, String> parameters,
                                                   final boolean successFullAuthentication,
                                                   final String id,
                                                   final ParameterList parameterList) {
        val response = serverManager.authResponse(parameterList, id, id, successFullAuthentication, true);
        parameters.putAll(response.getParameterMap());
        LOGGER.debug("Parameters passed for the OpenID response are [{}]", parameters.keySet());
        return buildRedirect(service, parameters);
    }

    /**
     * Gets association.
     *
     * @param serverManager the server manager
     * @param parameterList the parameter list
     * @return the association
     */
    protected Association getAssociation(final ServerManager serverManager, final ParameterList parameterList) {
        try {
            val authReq = AuthRequest.createAuthRequest(parameterList, serverManager.getRealmVerifier());
            val parameterMap = authReq.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                val assocHandle = (String) parameterMap.get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
                if (assocHandle != null) {
                    return serverManager.getSharedAssociations().load(assocHandle);
                }
            }
        } catch (final MessageException e) {
            LOGGER.error("Message exception : [{}]", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean supports(final WebApplicationService service) {
        return service instanceof OpenIdService;
    }

    /**
     * Is association valid.
     *
     * @param association the association
     * @return true/false
     */
    protected boolean isAssociationValid(final Association association) {
        return association != null && !association.hasExpired();
    }
}
