package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.util.List;

/**
 * The {@link SSOPostProfileHandlerController} is responsible for
 * handling profile requests for SAML2 Web SSO.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Controller("ssoPostProfileHandlerController")
public class SSOPostProfileHandlerController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();

    @Autowired
    private ParserPool parserPool;

    @NotNull
    @Value("${server.name}")
    private String casServerName;

    @NotNull
    @Value("${server.prefix}")
    private String casServerPrefix;

    @NotNull
    @Value("${server.prefix}/login")
    private String casServerLoginUrl;

    @Autowired
    @Qualifier("servicesManager")
    private ReloadableServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private Service callbackService;

    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    private SamlProfileSamlResponseBuilder responseBuilder;

    @Autowired
    private VelocityEngineFactory velocityEngineFactory;

    /**
     * Instantiates a new redirect profile handler controller.
     */
    public SSOPostProfileHandlerController() {}

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    protected void initialize() {
        this.callbackService = webApplicationServiceFactory.createService(
                this.casServerPrefix.concat(SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK));
        logger.debug("Initialized callback service url {}", this.callbackService);

        if (!servicesManager.matchesExistingService(this.callbackService))  {
            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(new SecureRandom().nextLong());
            service.setName(service.getClass().getSimpleName());
            service.setDescription(SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK.concat(" Callback Url"));
            service.setServiceId(this.callbackService.getId());

            logger.debug("Saving callback service {} into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.reload();
        }
    }

    private static void storeAuthnRequest(final HttpServletRequest request, final AuthnRequest authnRequest) {
        final HttpSession session = request.getSession();
        session.setAttribute(AuthnRequest.class.getName(), authnRequest);
    }

    private static AuthnRequest retrieveAuthnRequest(final HttpServletRequest request) {
        final HttpSession session = request.getSession();
        return (AuthnRequest) session.getAttribute(AuthnRequest.class.getName());
    }

    /**
     * Handle callback profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @RequestMapping(path = SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK, method = RequestMethod.GET)
    protected void handleCallbackProfileRequest(final HttpServletResponse response,
                                        final HttpServletRequest request) throws Exception {
        try {
            logger.info("Received SAML callback profile request {}", request.getRequestURI());
            final AuthnRequest authnRequest = retrieveAuthnRequest(request);
            if (authnRequest == null) {
                logger.error("Can not validate the request because the original Authn request can not be found.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            final String ticket = CommonUtils.safeGetParameter(request, CasProtocolConstants.PARAMETER_TICKET);
            if (StringUtils.isBlank(ticket)) {
                logger.error("Can not validate the request because no {} is provided via the request",
                        CasProtocolConstants.PARAMETER_TICKET);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            final Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(this.casServerPrefix);
            validator.setRenew(authnRequest.isForceAuthn());
            final String serviceUrl = constructServiceUrl(request, response,
                    this.callbackService.getId(), this.casServerName);
            logger.debug("Created service url for validation: {}", serviceUrl);
            final Assertion assertion = validator.validate(ticket, serviceUrl);
            logValidationAssertion(assertion);
            if (assertion.isValid()) {
                final SamlRegisteredService registeredService = getRegisteredServiceAndVerify(authnRequest);
                logger.debug("Preparing SAML response to {}", registeredService);

                final SamlMetadataAdaptor adaptor = SamlMetadataAdaptor.adapt(registeredService,
                        getAssertionConsumerServiceFor(authnRequest));

                final Response samlResponse = responseBuilder.build(authnRequest, request,
                        response, assertion, registeredService, adaptor);
                logger.info("Built the SAML response for {}. Encoding...", registeredService.getEntityId());
                encodeSamlResponse(registeredService, samlResponse, response, adaptor);
            }
        } finally {
            storeAuthnRequest(request, null);
        }
    }

    private void encodeSamlResponse(final SamlRegisteredService service, final Response samlResponse,
                                    final HttpServletResponse httpResponse, final SamlMetadataAdaptor adaptor) throws Exception {
        final List<AssertionConsumerService> assertionConsumerServices = adaptor.getAssertionConsumerServices();

        if (assertionConsumerServices.isEmpty()) {
            throw new SamlException(SamlException.CODE, "No assertion consumer services could be found in the metadata for service "
                + service.getServiceId() + " and metadata location " + service.getMetadataLocation());
        }

        final HTTPPostEncoder encoder = new HTTPPostEncoder();
        encoder.setHttpServletResponse(httpResponse);
        encoder.setVelocityEngine(this.velocityEngineFactory.createVelocityEngine());
        final MessageContext outboundMessageContext = new MessageContext<>();
        final SAMLPeerEntityContext peerEntityContext = outboundMessageContext.getSubcontext(SAMLPeerEntityContext.class, true);
        if (peerEntityContext != null) {
            final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
            if (endpointContext != null) {
                final AssertionConsumerService assertionConsumerService = assertionConsumerServices.get(0);
                logger.info("Encoding SAML response for endpoint {} and binding {}", assertionConsumerService.getLocation(),
                        assertionConsumerService.getBinding());
                endpointContext.setEndpoint(assertionConsumerServices.get(0));

            }
        }

        outboundMessageContext.setMessage(samlResponse);
        encoder.setMessageContext(outboundMessageContext);
        encoder.initialize();
        encoder.encode();
    }

    private AssertionConsumerService getAssertionConsumerServiceFor(final AuthnRequest authnRequest) {
        final AssertionConsumerService acs = new AssertionConsumerServiceBuilder().buildObject();
        acs.setBinding(authnRequest.getProtocolBinding());
        acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
        acs.setResponseLocation(authnRequest.getAssertionConsumerServiceURL());
        return acs;
    }

    private SamlRegisteredService getRegisteredServiceAndVerify(final AuthnRequest authnRequest) {
        final String serviceId = authnRequest.getAssertionConsumerServiceURL();
        final RegisteredService registeredService =
                this.servicesManager.findServiceBy(this.webApplicationServiceFactory.createService(serviceId));
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }

        if (registeredService instanceof SamlRegisteredService) {
            final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) registeredService;
            logger.debug("Located SAML service in the registry as {} with the metadata location of {}",
                    samlRegisteredService.getServiceId(), samlRegisteredService.getMetadataLocation());
            return samlRegisteredService;
        }
        logger.error("Service {} is found in registry but it is not a SAML service", serviceId);
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request the request
     * @throws Exception the exception
     */
    @RequestMapping(path= SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST, method = RequestMethod.POST)
    protected void handleProfileRequest(final HttpServletResponse response,
                                        final HttpServletRequest request) throws Exception {
        logger.info("Received SAML profile request {}", request.getRequestURI());

        final SAMLObject messageContext = decodeAuthnRequest(request);
        if (messageContext instanceof AuthnRequest) {
            final AuthnRequest authnRequest = (AuthnRequest) messageContext;
            getRegisteredServiceAndVerify(authnRequest);
            storeAuthnRequest(request, authnRequest);
            logAuthnRequest(authnRequest);
            performAuthentication(authnRequest, request, response);
        }
    }

    private void logValidationAssertion(final Assertion assertion) {
        logger.debug("CAS Assertion Valid: {}", assertion.isValid());
        logger.debug("CAS Assertion Principal: {}", assertion.getPrincipal().getName());
        logger.debug("CAS Assertion AuthN Date: {}", assertion.getAuthenticationDate());
        logger.debug("CAS Assertion ValidFrom Date: {}", assertion.getValidFromDate());
        logger.debug("CAS Assertion ValidUntil Date: {}", assertion.getValidUntilDate());
        logger.debug("CAS Assertion Attributes: {}", assertion.getAttributes());
        logger.debug("CAS Assertion Principal Attributes: {}", assertion.getPrincipal().getAttributes());
    }

    private void performAuthentication(final AuthnRequest authnRequest,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response) throws Exception {
        final String serviceUrl = constructServiceUrl(request, response, this.callbackService.getId(),
                this.casServerName);
        logger.debug("Created service url {}", serviceUrl);

        final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl,
                CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
                authnRequest.isPassive());

        logger.debug("Redirecting SAML authN request to \"{}\"", urlToRedirectTo);
        this.authenticationRedirectStrategy.redirect(request, response, urlToRedirectTo);

    }

    private SAMLObject decodeAuthnRequest(final HttpServletRequest request) {
        try {
            final HTTPPostDecoder decoder = new HTTPPostDecoder();
            decoder.setHttpServletRequest(request);
            decoder.setParserPool(this.parserPool);
            decoder.initialize();
            decoder.decode();
            return decoder.getMessageContext().getMessage();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void logAuthnRequest(final AuthnRequest authnRequest) {
        logger.debug("\t Request Issuer: {}", authnRequest.getIssuer().getValue());
        logger.debug("\t AssertionConsumerServiceURL: {}", authnRequest.getAssertionConsumerServiceURL());
        logger.debug("\t Destination: {}", authnRequest.getDestination());
        logger.debug("\t ProtocolBinding: {}", authnRequest.getProtocolBinding());
        logger.debug("\t Forced AuthN: {}", authnRequest.isForceAuthn());
        logger.debug("\t Passive AuthN: {}", authnRequest.isPassive());
        logger.debug("\t Signed AuthN: {}", authnRequest.isSigned());

        if (StringUtils.isNotBlank(authnRequest.getProviderName())) {
            logger.debug("\t ProviderName: {}", authnRequest.getProviderName());
        }
        logger.debug("\t IssueInstant: {}", authnRequest.getIssueInstant());

        if (authnRequest.getRequestedAuthnContext() != null) {
            for (final AuthnContextClassRef ctx : authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs()) {
                logger.debug("\t AuthnContextClassRef: {}", ctx.getAuthnContextClassRef());
            }

            for (final AuthnContextDeclRef ctx : authnRequest.getRequestedAuthnContext().getAuthnContextDeclRefs()) {
                logger.debug("\t AuthnContextClassRef: {}", ctx.getAuthnContextDeclRef());
            }
            logger.debug("\t AuthnContextClass Comparison: {}",
                    authnRequest.getRequestedAuthnContext().getComparison());
        }

        if (authnRequest.getNameIDPolicy() != null) {
            logger.debug("\t NameIDFormat: {}", authnRequest.getNameIDPolicy().getFormat());
            logger.debug("\t SPNameQualifier: {}", authnRequest.getNameIDPolicy().getSPNameQualifier());
        }
    }


    private static String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                       final String service, final String serverName) {
        return CommonUtils.constructServiceUrl(request, response,
                service, serverName,
                CasProtocolConstants.PARAMETER_SERVICE,
                CasProtocolConstants.PARAMETER_TICKET, false);
    }
}
