package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SSOSamlIdPProfileCallbackHandlerController}, which handles
 * the profile callback request to build the final saml response.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SSOSamlIdPProfileCallbackHandlerController extends AbstractSamlIdPProfileHandlerController {

    public SSOSamlIdPProfileCallbackHandlerController(final SamlProfileHandlerConfigurationContext samlProfileHandlerConfigurationContext) {
        super(samlProfileHandlerConfigurationContext);
    }

    /**
     * Build authentication context pair pair.
     *
     * @param request      the request
     * @param authnRequest the authn request
     * @return the pair
     */
    protected static Pair<AuthnRequest, MessageContext> buildAuthenticationContextPair(final HttpServletRequest request,
                                                                                       final AuthnRequest authnRequest) {
        val messageContext = bindRelayStateParameter(request);
        return Pair.of(authnRequest, messageContext);
    }

    private static MessageContext bindRelayStateParameter(final HttpServletRequest request) {
        val messageContext = new MessageContext();
        val relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);
        LOGGER.trace("Relay state is [{}]", relayState);
        SAMLBindingSupport.setRelayState(messageContext, relayState);
        return messageContext;
    }

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK)
    protected void handleCallbackProfileRequest(final HttpServletResponse response,
                                                final HttpServletRequest request) throws Exception {
        LOGGER.info("Received SAML callback profile request [{}]", request.getRequestURI());
        val authnRequest = retrieveSamlAuthenticationRequestFromHttpRequest(request);
        if (authnRequest == null) {
            LOGGER.error("Can not validate the request because the original Authn request can not be found.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request",
                CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        val authenticationContext = buildAuthenticationContextPair(request, authnRequest);
        val assertion = validateRequestAndBuildCasAssertion(response, request, authenticationContext);
        val binding = determineProfileBinding(authenticationContext, assertion);
        buildSamlResponse(response, request, authenticationContext, assertion, binding);
    }

    private Assertion validateRequestAndBuildCasAssertion(final HttpServletResponse response,
                                                          final HttpServletRequest request,
                                                          final Pair<AuthnRequest, MessageContext> pair) throws Exception {
        val authnRequest = pair.getKey();
        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        getSamlProfileHandlerConfigurationContext().getTicketValidator().setRenew(authnRequest.isForceAuthn());
        val serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        val assertion = getSamlProfileHandlerConfigurationContext().getTicketValidator().validate(ticket, serviceUrl);
        logCasValidationAssertion(assertion);
        return assertion;
    }

    /**
     * Determine profile binding.
     *
     * @param authenticationContext the authentication context
     * @param assertion             the assertion
     * @return the string
     */
    protected String determineProfileBinding(final Pair<AuthnRequest, MessageContext> authenticationContext,
                                             final Assertion assertion) {

        val authnRequest = authenticationContext.getKey();
        val pair = getRegisteredServiceAndFacade(authnRequest);
        val facade = pair.getValue();

        val binding = StringUtils.defaultIfBlank(authnRequest.getProtocolBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        LOGGER.debug("Determined authentication request binding is [{}], issued by [{}]",
            binding, authnRequest.getIssuer().getValue());

        val entityId = facade.getEntityId();
        LOGGER.debug("Checking metadata for [{}] to see if binding [{}] is supported", entityId, binding);
        val svc = facade.getAssertionConsumerService(binding);
        LOGGER.debug("Binding [{}] is supported by [{}]", svc.getBinding(), entityId);
        return binding;
    }
}
