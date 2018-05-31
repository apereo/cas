package org.apereo.cas.support.saml.web.idp.profile.sso;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.AbstractSamlProfileHandlerController;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
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
@Slf4j
public class SSOSamlProfileCallbackHandlerController extends AbstractSamlProfileHandlerController {

    private final AbstractUrlBasedTicketValidator ticketValidator;

    public SSOSamlProfileCallbackHandlerController(final SamlIdPObjectSigner samlObjectSigner,
                                                   final ParserPool parserPool,
                                                   final AuthenticationSystemSupport authenticationSystemSupport,
                                                   final ServicesManager servicesManager,
                                                   final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final OpenSamlConfigBean configBean,
                                                   final SamlProfileObjectBuilder<Response> responseBuilder,
                                                   final CasConfigurationProperties casProperties,
                                                   final SamlObjectSignatureValidator samlObjectSignatureValidator,
                                                   final AbstractUrlBasedTicketValidator ticketValidator,
                                                   final Service callbackService) {
        super(samlObjectSigner,
            parserPool,
            authenticationSystemSupport,
            servicesManager,
            webApplicationServiceFactory,
            samlRegisteredServiceCachingMetadataResolver,
            configBean,
            responseBuilder,
            casProperties,
            samlObjectSignatureValidator,
            callbackService);
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
        final var authnRequest = retrieveSamlAuthenticationRequestFromHttpRequest(request);
        if (authnRequest == null) {
            LOGGER.error("Can not validate the request because the original Authn request can not be found.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final var ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        if (StringUtils.isBlank(ticket)) {
            LOGGER.error("Can not validate the request because no [{}] is provided via the request", CasProtocolConstants.PARAMETER_TICKET);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final var authenticationContext = buildAuthenticationContextPair(request, authnRequest);
        final var assertion = validateRequestAndBuildCasAssertion(response, request, authenticationContext);
        final var binding = determineProfileBinding(authenticationContext, assertion);
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
        final var messageContext = bindRelayStateParameter(request);
        return Pair.of(authnRequest, messageContext);
    }

    private static MessageContext<SAMLObject> bindRelayStateParameter(final HttpServletRequest request) {
        final MessageContext<SAMLObject> messageContext = new MessageContext<>();
        final var relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);
        LOGGER.debug("Relay state is [{}]", relayState);
        SAMLBindingSupport.setRelayState(messageContext, relayState);
        return messageContext;
    }

    private Assertion validateRequestAndBuildCasAssertion(final HttpServletResponse response,
                                                          final HttpServletRequest request,
                                                          final Pair<AuthnRequest, MessageContext> pair) throws Exception {
        final var authnRequest = pair.getKey();
        final var ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
        this.ticketValidator.setRenew(authnRequest.isForceAuthn());
        final var serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.trace("Created service url for validation: [{}]", serviceUrl);
        final var assertion = this.ticketValidator.validate(ticket, serviceUrl);
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

        final var authnRequest = authenticationContext.getKey();
        final var pair = getRegisteredServiceAndFacade(authnRequest);
        final var facade = pair.getValue();

        final var binding = StringUtils.defaultIfBlank(authnRequest.getProtocolBinding(), SAMLConstants.SAML2_POST_BINDING_URI);
        LOGGER.debug("Determined authentication request binding is [{}], issued by [{}]", binding, authnRequest.getIssuer().getValue());

        final var entityId = facade.getEntityId();
        LOGGER.debug("Checking metadata for [{}] to see if binding [{}] is supported", entityId, binding);
        @NonNull
        final var svc = facade.getAssertionConsumerService(binding);
        LOGGER.debug("Binding [{}] is supported by [{}]", svc.getBinding(), entityId);
        return binding;
    }
}
