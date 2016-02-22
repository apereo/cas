package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.collection.Pair;
import net.shibboleth.utilities.java.support.net.URLBuilder;
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
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.SamlIdPConstants;
import org.jasig.cas.support.saml.SamlIdPUtils;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.jasig.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.jasig.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.jasig.cas.util.EncodingUtils;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.SecureRandom;

/**
 * A parent controller to handle SAML requests.
 * Specific profile endpoints are handled by extensions.
 * This parent provides the necessary ops for profile endpoint
 * controllers to respond to end points.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public abstract class AbstractSamlProfileHandlerController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Saml object signer.
     */
    @Autowired
    @Qualifier("samlObjectSigner")
    protected SamlObjectSigner samlObjectSigner;

    /**
     * The Parser pool.
     */
    @Autowired
    protected ParserPool parserPool;

    /**
     * Callback service.
     */
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
     * The Saml registered service caching metadata resolver.
     */
    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    /**
     * The Config bean.
     */
    @Autowired
    protected OpenSamlConfigBean configBean;


    /**
     * The Response builder.
     */
    @Autowired
    @Qualifier("samlProfileSamlResponseBuilder")
    protected SamlProfileSamlResponseBuilder responseBuilder;

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    protected void initialize() {
        this.callbackService = registerCallback(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK);
    }

    /**
     * Gets saml metadata adaptor for service.
     *
     * @param registeredService the registered service
     * @param authnRequest      the authn request
     * @return the saml metadata adaptor for service
     */
    protected SamlRegisteredServiceServiceProviderMetadataFacade getSamlMetadataFacadeFor(final SamlRegisteredService registeredService,
                                                                                          final AuthnRequest authnRequest) {
        return SamlRegisteredServiceServiceProviderMetadataFacade
                .get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, authnRequest);
    }


    /**
     * Gets registered service and verify.
     *
     * @param serviceId the service id
     * @return the registered service and verify
     */
    protected SamlRegisteredService verifySamlRegisteredService(final String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Could not verify/locate SAML registered service since no serviceId is provided");
        }
        logger.debug("Checking service access in CAS service registry for [{}]", serviceId);
        final RegisteredService registeredService =
                this.servicesManager.findServiceBy(this.webApplicationServiceFactory.createService(serviceId));
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            logger.warn("[{}] is not found in the registry or service access is denied. Ensure service is registered in service registry",
                    serviceId);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }

        if (registeredService instanceof SamlRegisteredService) {
            final SamlRegisteredService samlRegisteredService = (SamlRegisteredService) registeredService;
            logger.debug("Located SAML service in the registry as [{}] with the metadata location of [{}]",
                    samlRegisteredService.getServiceId(), samlRegisteredService.getMetadataLocation());
            return samlRegisteredService;
        }
        logger.error("Service [{}] is found in registry but it is not defined as a SAML service", serviceId);
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
        logger.debug("Initialized callback service [{}]", callbackService);

        if (!servicesManager.matchesExistingService(callbackService)) {
            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(new SecureRandom().nextLong());
            service.setName(service.getClass().getSimpleName());
            service.setDescription(SamlIdPConstants.ENDPOINT_SAML2_SSO_PROFILE_POST_CALLBACK.concat(" Callback Url"));
            service.setServiceId(callbackService.getId());

            logger.debug("Saving callback service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.reload();
        }
        return callbackService;
    }

    /**
     * Retrieve authn request authn request.
     *
     * @param request the request
     * @return the authn request
     * @throws Exception the exception
     */
    protected AuthnRequest retrieveAuthnRequest(final HttpServletRequest request) throws Exception {
        logger.debug("Retrieving authentication request from scope");
        final String requestValue = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
        final byte[] encodedRequest = EncodingUtils.decodeBase64(requestValue.getBytes("UTF-8"));
        final AuthnRequest authnRequest = (AuthnRequest) 
                XMLObjectSupport.unmarshallFromInputStream(configBean.getParserPool(), new ByteArrayInputStream(encodedRequest));
        return authnRequest;
    }

    /**
     * Decode authentication request saml object.
     *
     * @param <T>     the type parameter
     * @param request the request
     * @param decoder the decoder
     * @param clazz   the clazz
     * @return the saml object
     */
    protected <T extends SignableSAMLObject> T decodeRequest(final HttpServletRequest request,
                                                             final BaseHttpServletRequestXMLMessageDecoder decoder,
                                                             final Class<? extends SignableSAMLObject> clazz) {
        logger.info("Received SAML profile request [{}]", request.getRequestURI());

        try {
            decoder.setHttpServletRequest(request);
            decoder.setParserPool(this.parserPool);
            decoder.initialize();
            decoder.decode();
            final SignableSAMLObject object = (SignableSAMLObject) decoder.getMessageContext().getMessage();

            if (object == null) {
                return null;
            }

            if (!clazz.isAssignableFrom(object.getClass())) {
                throw new ClassCastException("SAML object [" + object.getClass().getName() + " type does not match " + clazz);
            }

            logger.debug("Decoded SAML object [{}] from http request", object.getElementQName());
            return (T) object;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Log cas validation assertion.
     *
     * @param assertion the assertion
     */
    protected void logCasValidationAssertion(final Assertion assertion) {
        logger.debug("CAS Assertion Valid: [{}]", assertion.isValid());
        logger.debug("CAS Assertion Principal: [{}]", assertion.getPrincipal().getName());
        logger.debug("CAS Assertion AuthN Date: [{}]", assertion.getAuthenticationDate());
        logger.debug("CAS Assertion ValidFrom Date: [{}]", assertion.getValidFromDate());
        logger.debug("CAS Assertion ValidUntil Date: [{}]", assertion.getValidUntilDate());
        logger.debug("CAS Assertion Attributes: [{}]", assertion.getAttributes());
        logger.debug("CAS Assertion Principal Attributes: [{}]", assertion.getPrincipal().getAttributes());
    }

    /**
     * Redirect request for authentication.
     *
     * @param authnRequest the authn request
     * @param request      the request
     * @param response     the response
     * @throws Exception the exception
     */
    protected void issueAuthenticationRequestRedirect(final AuthnRequest authnRequest,
                                                      final HttpServletRequest request,
                                                      final HttpServletResponse response) throws Exception {
        final String serviceUrl = constructServiceUrl(request, response, authnRequest);
        logger.debug("Created service url [{}]", serviceUrl);

        final String urlToRedirectTo = CommonUtils.constructRedirectUrl(this.casServerLoginUrl,
                CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
                authnRequest.isPassive());

        logger.debug("Redirecting SAML authN request to \"[{}]\"", urlToRedirectTo);
        final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, urlToRedirectTo);

    }

    /**
     * Construct service url string.
     *
     * @param request      the request
     * @param response     the response
     * @param authnRequest the authn request
     * @return the string
     * @throws SamlException the saml exception
     */
    protected String constructServiceUrl(final HttpServletRequest request, 
                                         final HttpServletResponse response,
                                         final AuthnRequest authnRequest)
            throws SamlException {
        try (final StringWriter writer = SamlIdPUtils.transformSamlObject(this.configBean, authnRequest)) {
            final URLBuilder builder = new URLBuilder(this.callbackService.getId());
            builder.getQueryParams().add(new Pair<>(SamlProtocolConstants.PARAMETER_ENTITY_ID, authnRequest.getIssuer().getValue()));

            final String samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes("UTF-8"));
            builder.getQueryParams().add(new Pair<>(SamlProtocolConstants.PARAMETER_SAML_REQUEST, samlRequest));
            final String url = builder.buildURL();

            logger.debug("Built service callback url [{}]", url);
            return CommonUtils.constructServiceUrl(request, response,
                    url, this.casServerName,
                    CasProtocolConstants.PARAMETER_SERVICE,
                    CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }
}

