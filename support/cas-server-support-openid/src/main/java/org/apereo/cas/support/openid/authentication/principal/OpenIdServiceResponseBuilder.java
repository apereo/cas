package org.apereo.cas.support.openid.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.validation.Assertion;
import org.openid4java.association.Association;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds responses to Openid authN requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdServiceResponseBuilder.class);

    private static final long serialVersionUID = -4581238964007702423L;

    private final ServerManager serverManager;

    private final CentralAuthenticationService centralAuthenticationService;

    private final String openIdPrefixUrl;

    /**
     * Instantiates a new Open id service response builder.
     *
     * @param openIdPrefixUrl              the open id prefix url
     * @param serverManager                the server manager
     * @param centralAuthenticationService the central authentication service
     */
    public OpenIdServiceResponseBuilder(final String openIdPrefixUrl,
                                        final ServerManager serverManager,
                                        final CentralAuthenticationService centralAuthenticationService) {
        this.openIdPrefixUrl = openIdPrefixUrl;
        this.serverManager = serverManager;
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

        final OpenIdService service = (OpenIdService) webApplicationService;
        final ParameterList parameterList = new ParameterList(HttpRequestUtils.getHttpServletRequestFromRequestAttributes().getParameterMap());

        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.isBlank(ticketId)) {
            parameters.put(OpenIdProtocolConstants.OPENID_MODE, OpenIdProtocolConstants.CANCEL);
            return buildRedirect(service, parameters);
        }

        final Association association = getAssociation(serverManager, parameterList);
        final boolean associated = association != null;
        final boolean associationValid = isAssociationValid(association);
        boolean successFullAuthentication = true;

        Assertion assertion = null;
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
        final String id = determineIdentity(service, assertion);
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
        final String id;
        if (assertion != null && OpenIdProtocolConstants.OPENID_IDENTIFIERSELECT.equals(service.getIdentity())) {
            id = this.openIdPrefixUrl + '/' + assertion.getPrimaryAuthentication().getPrincipal().getId();
        } else {
            id = service.getIdentity();
        }
        return id;
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
        final Message response = serverManager.authResponse(parameterList, id, id, successFullAuthentication, true);
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
            final AuthRequest authReq = AuthRequest.createAuthRequest(parameterList, serverManager.getRealmVerifier());
            final Map parameterMap = authReq.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                final String assocHandle = (String) parameterMap.get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
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
