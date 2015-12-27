package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegexRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ReloadableServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextDeclRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.velocity.VelocityEngineFactory;

import javax.servlet.http.HttpServletRequest;
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
     * The Velocity engine factory.
     */
    @Autowired
    protected VelocityEngineFactory velocityEngineFactory;

    /**
     * The Web application service factory.
     */
    @Autowired
    @Qualifier("webApplicationServiceFactory")
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

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
        session.setAttribute(AuthnRequest.class.getName(), authnRequest);
    }

    /**
     * Retrieve authn request authn request.
     *
     * @param request the request
     * @return the authn request
     */
    protected AuthnRequest retrieveAuthnRequest(final HttpServletRequest request) {
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
                                                     final HTTPPostDecoder decoder) {
        try {
            decoder.setHttpServletRequest(request);
            decoder.setParserPool(this.parserPool);
            decoder.initialize();
            decoder.decode();
            return (AuthnRequest) decoder.getMessageContext().getMessage();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
