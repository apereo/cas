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
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;

/**
 * This is {@link AbstractSamlProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public abstract class AbstractSamlProfileHandlerController {
    /**
     * The Logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Parser pool.
     */
    @Autowired
    protected ParserPool parserPool;

    /** Callback service. */
    protected Service callbackService;

    /**
     * The Cas server name.
     */
    @NotNull
    @Value("${server.name}")
    protected String casServerName;

    /**
     * The Cas server prefix.
     */
    @NotNull
    @Value("${server.prefix}")
    protected String casServerPrefix;

    /**
     * The Cas server login url.
     */
    @NotNull
    @Value("${server.prefix}/login")
    protected String casServerLoginUrl;

    /**
     * The Services manager.
     */
    @Autowired
    @Qualifier("servicesManager")
    protected ReloadableServicesManager servicesManager;

    /**
     * The Web application service factory.
     */
    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;


    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    protected void initialize() {
        this.callbackService = registerCallback(SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK);
    }

    /**
     * Log authn request.
     *
     * @param authnRequest the authn request
     */
    protected void logAuthnRequest(final AuthnRequest authnRequest) {
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

    /**
     * Gets registered service and verify.
     *
     * @param authnRequest the authn request
     * @return the registered service and verify
     */
    protected SamlRegisteredService verifySamlRegisteredService(final AuthnRequest authnRequest) {
        final String serviceId = authnRequest.getAssertionConsumerServiceURL();
        logger.debug("Checking service access in CAS service registry for {}", serviceId);
        final RegisteredService registeredService =
                this.servicesManager.findServiceBy(this.webApplicationServiceFactory.createService(serviceId));
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            logger.warn("{} is not found in the registry or service access is denied. Ensure service is registered in the CAS registry",
                    serviceId);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }

        if (registeredService instanceof SamlRegisteredService) {
            final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) registeredService;
            logger.debug("Located SAML service in the registry as {} with the metadata location of {}",
                    samlRegisteredService.getServiceId(), samlRegisteredService.getMetadataLocation());
            return samlRegisteredService;
        }
        logger.error("Service {} is found in registry but it is not defined as a SAML service", serviceId);
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
    }

    /**
     * Initialize callback service.
     *
     * @param callbackUrl the callback url
     * @return the service
     */
    protected Service registerCallback(final String callbackUrl) {
        final Service callbackService = webApplicationServiceFactory.createService(this.casServerPrefix.concat(callbackUrl));
        logger.debug("Initialized callback service {}", callbackService);

        if (!servicesManager.matchesExistingService(callbackService))  {
            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(new SecureRandom().nextLong());
            service.setName(service.getClass().getSimpleName());
            service.setDescription(SamlProtocolConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK.concat(" Callback Url"));
            service.setServiceId(callbackService.getId());

            logger.debug("Saving callback service {} into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.reload();
        }
        return callbackService;
    }

    /**
     * Store authn request.
     *
     * @param request      the request
     * @param authnRequest the authn request
     */
    protected void storeAuthnRequest(final HttpServletRequest request, final AuthnRequest authnRequest) {
        final HttpSession session = request.getSession();
        if (authnRequest != null) {
            logger.debug("Storing authentication request issued by {} into session as {}",
                    authnRequest.getIssuer().getValue(), AuthnRequest.class.getName());
        } else {
            logger.debug("Removing authentication request from session");
        }
        session.setAttribute(AuthnRequest.class.getName(), authnRequest);
    }

    /**
     * Retrieve authn request authn request.
     *
     * @param request the request
     * @return the authn request
     */
    protected AuthnRequest retrieveAuthnRequest(final HttpServletRequest request) {
        logger.debug("Retrieving authentication request from session");
        final HttpSession session = request.getSession();
        return (AuthnRequest) session.getAttribute(AuthnRequest.class.getName());
    }

    /**
     * Decode authentication request saml object.
     *
     * @param request the request
     * @param decoder the decoder
     * @return the saml object
     */
    protected AuthnRequest decodeAuthenticationRequest(final HttpServletRequest request,
                                                     final BaseHttpServletRequestXMLMessageDecoder<AuthnRequest> decoder) {
        try {
            decoder.setHttpServletRequest(request);
            decoder.setParserPool(this.parserPool);
            decoder.initialize();
            decoder.decode();
            final AuthnRequest authnRequest = decoder.getMessageContext().getMessage();
            logger.debug("Decoded authentication request from http request");
            logAuthnRequest(authnRequest);
            return authnRequest;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @throws Exception the exception
     */
    protected void handleProfileRequest(final HttpServletResponse response,
                                        final HttpServletRequest request,
                                        final BaseHttpServletRequestXMLMessageDecoder decoder) throws Exception {
        logger.info("Received SAML profile request {}", request.getRequestURI());
        final AuthnRequest authnRequest = decodeAuthenticationRequest(request, decoder);
        verifySamlRegisteredService(authnRequest);
        storeAuthnRequest(request, authnRequest);
        logAuthnRequest(authnRequest);
        performAuthentication(authnRequest, request, response, callbackService);
    }

    /**
     * Log cas validation assertion.
     *
     * @param assertion the assertion
     */
    protected void logCasValidationAssertion(final Assertion assertion) {
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
                                       final HttpServletResponse response,
                                       final Service callbackService) throws Exception {
        final String serviceUrl = constructServiceUrl(request, response, callbackService.getId(),
                this.casServerName);
        logger.debug("Created service url {}", serviceUrl);

        final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl,
                CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
                authnRequest.isPassive());

        logger.debug("Redirecting SAML authN request to \"{}\"", urlToRedirectTo);
        final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, urlToRedirectTo);

    }

    /**
     * Construct service url string.
     *
     * @param request    the request
     * @param response   the response
     * @param service    the service
     * @param serverName the server name
     * @return the string
     */
    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                              final String service, final String serverName) {
        return CommonUtils.constructServiceUrl(request, response,
                service, serverName,
                CasProtocolConstants.PARAMETER_SERVICE,
                CasProtocolConstants.PARAMETER_TICKET, false);
    }
}
