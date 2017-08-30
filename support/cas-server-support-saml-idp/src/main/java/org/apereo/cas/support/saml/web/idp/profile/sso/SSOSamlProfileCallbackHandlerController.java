package org.apereo.cas.support.saml.web.idp.profile.sso;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SSOSamlProfileCallbackHandlerController}, which handles
 * the profile callback request to build the final saml response.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SSOSamlProfileCallbackHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSOSamlProfileCallbackHandlerController.class);

    private final AbstractUrlBasedTicketValidator ticketValidator;

    /**
     * Instantiates a new idp-sso post saml profile handler controller.
     *
     * @param samlObjectSigner                             the saml object signer
     * @param parserPool                                   the parser pool
     * @param authenticationSystemSupport                  the authentication system support
     * @param servicesManager                              the services manager
     * @param webApplicationServiceFactory                 the web application service factory
     * @param samlRegisteredServiceCachingMetadataResolver the saml registered service caching metadata resolver
     * @param configBean                                   the config bean
     * @param responseBuilder                              the response builder
     * @param casProperties                                the cas properties
     * @param samlObjectSignatureValidator                 the saml object signature validator
     * @param ticketValidator                              the ticket validator
     */
    public SSOSamlProfileCallbackHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                                   final ParserPool parserPool,
                                                   final AuthenticationSystemSupport authenticationSystemSupport,
                                                   final ServicesManager servicesManager,
                                                   final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final OpenSamlConfigBean configBean,
                                                   final SamlProfileObjectBuilder<Response> responseBuilder,
                                                   final CasConfigurationProperties casProperties,
                                                   final SamlObjectSignatureValidator samlObjectSignatureValidator,
                                                   final AbstractUrlBasedTicketValidator ticketValidator) {
        super(samlObjectSigner,
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean,
                responseBuilder,
                casProperties,
                samlObjectSignatureValidator);
        this.ticketValidator = ticketValidator;
    }

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK)
    protected void handleCallbackProfileRequest(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        LOGGER.info("Received SAML callback profile request [{}]", request.getRequestURI());
        final AuthnRequest authnRequest = retrieveSamlAuthenticationRequestFromHttpRequest(request);
        if (authnRequest == null) {
            LOGGER.error("Can not validate the request because the original Authn request can not be found.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final Pair<AuthnRequest, MessageContext> authenticationContext = buildAuthenticationContextPair(request, authnRequest);
        final Assertion assertion = validateRequestAndBuildCasAssertion(response, request, authenticationContext);
        final String binding = determineProfileBinding(authenticationContext, assertion);
        buildSamlResponse(response, request, authenticationContext, assertion, binding);
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
        final MessageContext<SAMLObject> messageContext = bindRelayStateParameter(request);
        return Pair.of(authnRequest, messageContext);
    }

    private static MessageContext<SAMLObject> bindRelayStateParameter(final HttpServletRequest request) {
        final MessageContext<SAMLObject> messageContext = new MessageContext<>();
        final String relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);
        LOGGER.debug("Relay state is [{}]", relayState);
        SAMLBindingSupport.setRelayState(messageContext, relayState);
        return messageContext;
    }

    private Assertion validateRequestAndBuildCasAssertion(final HttpServletResponse response,
                                                          final HttpServletRequest request,
                                                          final Pair<AuthnRequest, MessageContext> pair) throws Exception {
        final AuthnRequest authnRequest = pair.getKey();
        final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        this.ticketValidator.setRenew(authnRequest.isForceAuthn());
        final String serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        final Assertion assertion = this.ticketValidator.validate(ticket, serviceUrl);
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

        final AuthnRequest authnRequest = authenticationContext.getKey();
        final Pair<SamlRegisteredService, SamlRegisteredServiceServiceProviderMetadataFacade> pair = getRegisteredServiceAndFacade(authnRequest);
        final SamlRegisteredServiceServiceProviderMetadataFacade facade = pair.getValue();

        final String binding = StringUtils.defaultIfBlank(authnRequest.getProtocolBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        LOGGER.debug("Determined authentication request binding is [{}], issued by [{}]", binding, authnRequest.getIssuer().getValue());

        LOGGER.debug("Checking metadata for [{}] to see if binding [{}] is supported", facade.getEntityId(), binding);
        final AssertionConsumerService svc = facade.getAssertionConsumerService(binding);
        if (svc == null) {
            throw new IllegalArgumentException("Requested binding [{}] is not supported by entity id " + facade.getEntityId());
        }
        LOGGER.debug("Binding [{}] is supported by [{}]", binding, facade.getEntityId());
        return binding;
    }
}
