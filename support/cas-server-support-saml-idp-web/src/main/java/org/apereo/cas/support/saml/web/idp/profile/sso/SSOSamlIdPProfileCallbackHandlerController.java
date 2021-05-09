package org.apereo.cas.support.saml.web.idp.profile.sso;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPCoreProperties;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlIdPProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.SamlProfileHandlerConfigurationContext;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.pac4j.core.context.JEEContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

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

    public SSOSamlIdPProfileCallbackHandlerController(final SamlProfileHandlerConfigurationContext config) {
        super(config);
    }

    /**
     * Build authentication context pair pair.
     *
     * @param request      the request
     * @param response     the response
     * @param authnRequest the authn request
     * @return the pair
     */
    protected Pair<AuthnRequest, MessageContext> buildAuthenticationContextPair(final HttpServletRequest request,
                                                                                final HttpServletResponse response,
                                                                                final AuthnRequest authnRequest) {
        val messageContext = bindRelayStateParameter(request, response);
        return Pair.of(authnRequest, messageContext);
    }

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
    protected ModelAndView handleCallbackProfileRequestGet(final HttpServletResponse response,
                                                           final HttpServletRequest request) throws Exception {
        autoConfigureCookiePath(request);
        val properties = configurationContext.getCasProperties();
        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type == SamlIdPCoreProperties.SessionStorageTypes.BROWSER_SESSION_STORAGE
            && !request.getParameterMap().containsKey(BrowserSessionStorage.KEY_SESSION_STORAGE)) {
            return new ModelAndView(CasWebflowConstants.VIEW_ID_SESSION_STORAGE_READ);
        }
        return handleProfileRequest(response, request);
    }

    /**
     * Handle callback profile request post.
     *
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_CALLBACK)
    protected ModelAndView handleCallbackProfileRequestPost(final HttpServletResponse response,
                                                            final HttpServletRequest request) throws Exception {
        autoConfigureCookiePath(request);
        val properties = configurationContext.getCasProperties();
        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type == SamlIdPCoreProperties.SessionStorageTypes.BROWSER_SESSION_STORAGE) {
            val storage = request.getParameter(BrowserSessionStorage.KEY_SESSION_STORAGE);
            val context = new JEEContext(request, response);
            configurationContext.getSessionStore().buildFromTrackableSession(context, storage);
            return handleProfileRequest(response, request);
        }
        return WebUtils.produceErrorView(new IllegalArgumentException("Unable to build SAML response"));
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
        if (svc != null) {
            LOGGER.debug("Binding [{}] is supported by [{}]", svc.getBinding(), entityId);
            return binding;
        }
        LOGGER.warn("Checking determine profile binding for [{}]", entityId);
        return null;
    }

    private ModelAndView handleProfileRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        LOGGER.info("Received SAML callback profile request [{}]", request.getRequestURI());
        val authnRequest = SamlIdPUtils.retrieveSamlRequest(new JEEContext(request, response),
            configurationContext.getSessionStore(),
            configurationContext.getOpenSamlConfigBean(),
            AuthnRequest.class);

        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            return WebUtils.produceErrorView(new IllegalArgumentException("Unable to handle SAML request"));
        }

        val authenticationContext = buildAuthenticationContextPair(request, response, authnRequest);
        val assertion = validateRequestAndBuildCasAssertion(response, request, authenticationContext);
        val binding = determineProfileBinding(authenticationContext, assertion);
        if (StringUtils.isBlank(binding)) {
            LOGGER.error("Unable to determine profile binding");
            return WebUtils.produceErrorView(new IllegalArgumentException("Unable to determine profile binding"));
        }
        buildSamlResponse(response, request, authenticationContext, assertion, binding);
        return null;
    }

    private MessageContext bindRelayStateParameter(final HttpServletRequest request,
                                                   final HttpServletResponse response) {
        val messageContext = new MessageContext();
        val context = new JEEContext(request, response);
        val relayState = configurationContext.getSessionStore()
            .get(context, SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE).orElse(StringUtils.EMPTY).toString();
        LOGGER.trace("Relay state is [{}]", relayState);
        SAMLBindingSupport.setRelayState(messageContext, relayState);
        return messageContext;
    }

    private Assertion validateRequestAndBuildCasAssertion(final HttpServletResponse response,
                                                          final HttpServletRequest request,
                                                          final Pair<AuthnRequest, MessageContext> pair) throws Exception {
        val ticket = request.getParameter(CasProtocolConstants.PARAMETER_TICKET);
        val validator = getConfigurationContext().getTicketValidator();
        val serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        val assertion = validator.validate(ticket, serviceUrl);
        logCasValidationAssertion(assertion);
        return assertion;
    }
}
