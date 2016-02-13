package org.jasig.cas.support.openid.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.AbstractWebApplicationServiceResponseBuilder;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.util.ApplicationContextProvider;
import org.jasig.cas.validation.Assertion;
import org.openid4java.association.Association;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds responses to Openid authN requests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceResponseBuilder extends AbstractWebApplicationServiceResponseBuilder {

    private static final long serialVersionUID = -4581238964007702423L;

    private final ParameterList parameterList;
    private final String openIdPrefixUrl;

    /**
     * Instantiates a new Open id service response builder.
     * @param parameterList the parameter list
     * @param openIdPrefixUrl the open id prefix url
     */
    public OpenIdServiceResponseBuilder(final ParameterList parameterList,
                                        final String openIdPrefixUrl) {
        this.parameterList = parameterList;
        this.openIdPrefixUrl = openIdPrefixUrl;
    }

    /**
     * Generates an Openid response.
     * If no ticketId is found, response is negative.
     * If we have a ticket id, then we check if we have an association.
     * If so, we ask OpenId server manager to generate the answer according with the existing association.
     * If not, we send back an answer with the ticket id as association handle.
     * This will force the consumer to ask a verification, which will validate the service ticket.
     *
     * @param ticketId the service ticket to provide to the service.
     * @param webApplicationService web application service
     * @return the generated authentication answer
     */
    @Override
    public Response build(final WebApplicationService webApplicationService, final String ticketId) {
        final ServerManager serverManager = ApplicationContextProvider.getApplicationContext()
                .getBean("serverManager", ServerManager.class);
        final CentralAuthenticationService centralAuthenticationService = ApplicationContextProvider
                .getApplicationContext().getBean("centralAuthenticationService",
                CentralAuthenticationService.class);

        final OpenIdService service = (OpenIdService) webApplicationService;

        final Map<String, String> parameters = new HashMap<>();

        if (StringUtils.isBlank(ticketId)) {
            parameters.put(OpenIdProtocolConstants.OPENID_MODE, OpenIdProtocolConstants.CANCEL);
            return buildRedirect(service, parameters);
        }

        final Association association = getAssociation(serverManager);
        final boolean associated = association != null;
        final boolean associationValid = isAssociationValid(association);
        boolean successFullAuthentication = true;

        Assertion assertion = null;
        try {
            if (associated && associationValid) {
                assertion = centralAuthenticationService.validateServiceTicket(ticketId, service);
                logger.debug("Validated openid ticket {} for {}", ticketId, service);
            } else {
                logger.warn("Association does not exist or is not valid");
                successFullAuthentication = false;
            }
        } catch (final AbstractTicketException te) {
            logger.error("Could not validate ticket : {}", te.getMessage(), te);
            successFullAuthentication = false;
        }

        final String id = determineIdentity(service, assertion);

        return buildAuthenticationResponse(serverManager, ticketId, service, parameters, associated,
            successFullAuthentication, id);
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
     *
     * @param serverManager the server manager
     * @param ticketId the ticket id
     * @param service the service
     * @param parameters the parameters
     * @param associated the associated
     * @param successFullAuthentication the success full authentication
     * @param id the id
     * @return response response
     */
    protected Response buildAuthenticationResponse(final ServerManager serverManager,
                                                   final String ticketId, final OpenIdService service,
                                                   final Map<String, String> parameters,
                                                   final boolean associated, final boolean successFullAuthentication,
                                                   final String id) {

        final Message response = serverManager.authResponse(this.parameterList, id, id,
            successFullAuthentication, true);
        parameters.putAll(response.getParameterMap());
        if (!associated) {
            parameters.put(OpenIdProtocolConstants.OPENID_ASSOCHANDLE, ticketId);
        }
        return buildRedirect(service, parameters);
    }

    /**
     * Gets association.
     *
     * @param serverManager the server manager
     * @return the association
     */
    protected Association getAssociation(final ServerManager serverManager) {
        try {
            final AuthRequest authReq = AuthRequest.createAuthRequest(this.parameterList,
                serverManager.getRealmVerifier());
            final Map parameterMap = authReq.getParameterMap();
            if (parameterMap != null && !parameterMap.isEmpty()) {
                final String assocHandle = (String) parameterMap.get(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
                if (assocHandle != null) {
                    return serverManager.getSharedAssociations().load(assocHandle);
                }
            }
        } catch (final MessageException me) {
            logger.error("Message exception : {}", me.getMessage(), me);
        }
        return null;
    }

    /**
     * Is association valid.
     *
     * @param association the association
     * @return the boolean
     */
    protected boolean isAssociationValid(final Association association) {
        return association != null && !association.hasExpired();
    }
}
