package org.apereo.cas.ws.idp.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.SecurityTokenTicket;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.TicketValidationResult;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * This is {@link WSFederationValidateRequestCallbackController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Tag(name = "WS Federation")
public class WSFederationValidateRequestCallbackController extends BaseWSFederationRequestController {

    public WSFederationValidateRequestCallbackController(final WSFederationRequestConfigurationContext configurationContext) {
        super(configurationContext);
    }

    private static ModelAndView postResponseBackToRelyingParty(final String rpToken,
                                                               final WSFederationRequest fedRequest) {
        val postUrl = StringUtils.isNotBlank(fedRequest.wreply()) ? fedRequest.wreply() : fedRequest.wtrealm();

        val parameters = new HashMap<String, Object>();
        parameters.put(WSFederationConstants.WA, WSFederationConstants.WSIGNIN10);
        parameters.put(WSFederationConstants.WRESULT, StringEscapeUtils.unescapeHtml4(rpToken));
        parameters.put(WSFederationConstants.WTREALM, fedRequest.wtrealm());

        if (StringUtils.isNotBlank(fedRequest.wctx())) {
            parameters.put(WSFederationConstants.WCTX, fedRequest.wctx());
        }

        LOGGER.trace("Posting relying party token to [{}]", postUrl);
        return new ModelAndView(CasWebflowConstants.VIEW_ID_POST_RESPONSE,
            CollectionUtils.wrap("originalUrl", postUrl, "parameters", parameters));
    }

    /**
     * Handle federation request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Throwable the throwable
     */
    @GetMapping(path = WSFederationConstants.ENDPOINT_FEDERATION_REQUEST_CALLBACK)
    @Operation(summary = "Handle WS-Federation request callback")
    protected ModelAndView handleFederationRequest(final HttpServletResponse response,
                                                   final HttpServletRequest request) throws Throwable {
        val fedRequest = WSFederationRequest.of(request);
        LOGGER.debug("Received callback profile request [{}]", request.getRequestURI());

        val serviceUrl = constructServiceUrl(request, response, fedRequest);
        val targetService = getConfigContext().getServiceSelectionStrategy()
            .resolveServiceFrom(getConfigContext().getWebApplicationServiceFactory().createService(serviceUrl));
        targetService.getAttributes().put(WSFederationConstants.WREPLY, CollectionUtils.wrapList(fedRequest.wreply()));
        targetService.getAttributes().put(WSFederationConstants.WTREALM, CollectionUtils.wrapList(fedRequest.wtrealm()));
        targetService.getAttributes().put(WSFederationConstants.WCTX, CollectionUtils.wrapList(fedRequest.wctx()));
        val service = findAndValidateFederationRequestForRegisteredService(targetService, fedRequest);
        LOGGER.debug("Located matching service [{}]", service);

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_ERROR, new HashMap<>(), HttpStatus.FORBIDDEN);
        }

        val assertion = validateRequestAndBuildCasAssertion(response, request, fedRequest);
        val securityTokenReq = getSecurityTokenFromRequest(request);
        val securityToken = FunctionUtils.doIfNull(securityTokenReq,
                Unchecked.supplier(() -> {
                    LOGGER.debug("No security token is yet available. Invoking security token service to issue token");
                    return fetchSecurityTokenFromAssertion(assertion, targetService);
                }),
                () -> securityTokenReq)
            .get();
        addSecurityTokenTicketToRegistry(request, securityToken);
        val rpToken = produceRelyingPartyToken(request, targetService, fedRequest, securityToken, assertion);
        return postResponseBackToRelyingParty(rpToken, fedRequest);
    }

    private SecurityToken fetchSecurityTokenFromAssertion(final TicketValidationResult assertion, final Service targetService) throws Throwable {
        val principal = assertion.getPrincipal().getId();
        val token = getConfigContext().getSecurityTokenServiceTokenFetcher().fetch(targetService, principal);
        if (token.isEmpty()) {
            LOGGER.warn("No security token could be retrieved for service [{}] and principal [{}]", targetService, principal);
            throw UnauthorizedServiceException.denied("Denied: %s".formatted(targetService.getId()));
        }
        return token.get();
    }

    private void addSecurityTokenTicketToRegistry(final HttpServletRequest request,
                                                  final SecurityToken securityToken) throws Throwable {
        LOGGER.trace("Creating security token as a ticket to CAS ticket registry...");
        val ticketRegistry = getConfigContext().getTicketRegistry();
        val tgt = CookieUtils.getTicketGrantingTicketFromRequest(getConfigContext().getTicketGrantingTicketCookieGenerator(),
            ticketRegistry, request);
        val serializedToken = SerializationUtils.serialize(securityToken);

        val securityTokenTicketFactory = (SecurityTokenTicketFactory) getConfigContext().getTicketFactory().get(SecurityTokenTicket.class);
        val ticket = securityTokenTicketFactory.create(tgt, serializedToken);
        LOGGER.trace("Created security token ticket [{}]", ticket);
        ticketRegistry.addTicket(ticket);
        LOGGER.trace("Added security token as a ticket to CAS ticket registry...");
        ticketRegistry.updateTicket(tgt);
    }

    private String produceRelyingPartyToken(final HttpServletRequest request, final Service targetService,
                                            final WSFederationRequest fedRequest, final SecurityToken securityToken,
                                            final TicketValidationResult assertion) throws Exception {
        val service = findAndValidateFederationRequestForRegisteredService(targetService, fedRequest);
        LOGGER.debug("Located registered service [{}] to create relying-party tokens...", service);
        return getConfigContext().getRelyingPartyTokenProducer().produce(securityToken, service, fedRequest, request, assertion);
    }

    private TicketValidationResult validateRequestAndBuildCasAssertion(final HttpServletResponse response,
                                                                       final HttpServletRequest request,
                                                                       final WSFederationRequest fedRequest) throws Throwable {
        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        val serviceUrl = constructServiceUrl(request, response, fedRequest);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        val assertion = getConfigContext().getTicketValidator().validate(ticket, serviceUrl);
        LOGGER.debug("Located CAS assertion [{}]", assertion);
        return assertion;
    }

}
