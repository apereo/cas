package org.apereo.cas.support.saml.web.idp.profile;

import com.google.common.base.Throwables;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.Pair;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

/**
 * A parent controller to handle SAML requests.
 * Specific profile endpoints are handled by extensions.
 * This parent provides the necessary ops for profile endpoint
 * controllers to respond to end points.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller
public abstract class AbstractSamlProfileHandlerController {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * The Saml object signer.
     */
    protected SamlObjectSigner samlObjectSigner;

    /**
     * The Parser pool.
     */
    protected ParserPool parserPool;

    /**
     * Callback service.
     */
    protected Service callbackService;

    /**
     * The Services manager.
     */
    protected ServicesManager servicesManager;

    /**
     * The Web application service factory.
     */
    protected ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * The Saml registered service caching metadata resolver.
     */
    protected SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    /**
     * The Config bean.
     */
    protected OpenSamlConfigBean configBean;

    /**
     * The Response builder.
     */
    protected SamlProfileSamlResponseBuilder responseBuilder;

    /**
     * Maps authentication contexts to what CAS can support.
     */
    protected Map<String, String> authenticationContextClassMappings = new CaseInsensitiveMap<>();

    private String serverPrefix;

    private String serverName;

    private String authenticationContextRequestParameter;

    private String loginUrl;

    private String logoutUrl;

    private boolean forceSignedLogoutRequests;

    private boolean singleLogoutCallbacksDisabled;

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
     * Gets saml metadata adaptor for service.
     *
     * @param registeredService the registered service
     * @param entityId          the entity id
     * @return the saml metadata adaptor for service
     */
    protected SamlRegisteredServiceServiceProviderMetadataFacade getSamlMetadataFacadeFor(final SamlRegisteredService registeredService,
                                                                                          final String entityId) {
        return SamlRegisteredServiceServiceProviderMetadataFacade
                .get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, entityId);
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
        final Service callbackService = this.webApplicationServiceFactory.createService(
                this.serverPrefix.concat(callbackUrl.concat(".+")));
        logger.debug("Initialized callback service [{}]", callbackService);

        if (!this.servicesManager.matchesExistingService(callbackService)) {
            final RegexRegisteredService service = new RegexRegisteredService();
            service.setId(Math.abs(new SecureRandom().nextLong()));
            service.setEvaluationOrder(0);
            service.setName(service.getClass().getSimpleName());
            service.setDescription("SAML Authentication Request");
            service.setServiceId(callbackService.getId());

            logger.debug("Saving callback service [{}] into the registry", service);
            this.servicesManager.save(service);
            this.servicesManager.load();
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
        final byte[] encodedRequest = EncodingUtils.decodeBase64(requestValue.getBytes(StandardCharsets.UTF_8));
        final AuthnRequest authnRequest = (AuthnRequest)
                XMLObjectSupport.unmarshallFromInputStream(this.configBean.getParserPool(), new ByteArrayInputStream(encodedRequest));
        return authnRequest;
    }

    /**
     * Decode authentication request saml object.
     *
     * @param request the request
     * @param decoder the decoder
     * @param clazz   the clazz
     * @return the saml object
     */
    protected Pair<? extends SignableSAMLObject, MessageContext> decodeRequest(final HttpServletRequest request,
                                                                               final BaseHttpServletRequestXMLMessageDecoder decoder,
                                                                               final Class<? extends SignableSAMLObject> clazz) {
        logger.info("Received SAML profile request [{}]", request.getRequestURI());

        try {
            decoder.setHttpServletRequest(request);
            decoder.setParserPool(this.parserPool);
            decoder.initialize();
            decoder.decode();

            final MessageContext messageContext = decoder.getMessageContext();
            final SignableSAMLObject object = (SignableSAMLObject) messageContext.getMessage();

            if (object == null) {
                throw new SAMLException("No " + clazz.getName() + " could be found in this request context. Decoder has failed.");
            }

            if (!clazz.isAssignableFrom(object.getClass())) {
                throw new ClassCastException("SAML object [" + object.getClass().getName() + " type does not match " + clazz);
            }

            logger.debug("Decoded SAML object [{}] from http request", object.getElementQName());
            return new Pair<>(object, messageContext);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Log cas validation assertion.
     *
     * @param assertion the assertion
     */
    protected void logCasValidationAssertion(final Assertion assertion) {
        logger.info("CAS Assertion Valid: [{}]", assertion.isValid());
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

        final String initialUrl = CommonUtils.constructRedirectUrl(this.loginUrl,
                CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
                authnRequest.isPassive());

        final String urlToRedirectTo = buildRedirectUrlByRequestedAuthnContext(initialUrl, authnRequest, request);

        logger.debug("Redirecting SAML authN request to \"[{}]\"", urlToRedirectTo);
        final AuthenticationRedirectStrategy authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, urlToRedirectTo);

    }

    /**
     * Build redirect url by requested authn context.
     *
     * @param initialUrl   the initial url
     * @param authnRequest the authn request
     * @param request      the request
     * @return the redirect url
     */
    protected String buildRedirectUrlByRequestedAuthnContext(final String initialUrl, final AuthnRequest authnRequest,
                                                             final HttpServletRequest request) {

        if (authnRequest.getRequestedAuthnContext() == null
                || authenticationContextClassMappings == null
                || this.authenticationContextClassMappings.isEmpty()) {
            return initialUrl;
        }
        final Optional<AuthnContextClassRef> p =
                authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().stream().filter(ref -> {
                    final String clazz = ref.getAuthnContextClassRef();
                    return this.authenticationContextClassMappings.containsKey(clazz);
                }).findFirst();

        if (p.isPresent()) {
            final String mappedClazz = this.authenticationContextClassMappings.get(p.get().getAuthnContextClassRef());
            return initialUrl + '&' + this.authenticationContextRequestParameter + '=' + mappedClazz;
        }

        return initialUrl;
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
        try (StringWriter writer = SamlUtils.transformSamlObject(this.configBean, authnRequest)) {
            final URLBuilder builder = new URLBuilder(this.callbackService.getId());
            builder.getQueryParams().add(
                    new net.shibboleth.utilities.java.support.collection.Pair<>(SamlProtocolConstants.PARAMETER_ENTITY_ID,
                    SamlIdPUtils.getIssuerFromSamlRequest(authnRequest)));

            final String samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            builder.getQueryParams().add(
                    new net.shibboleth.utilities.java.support.collection.Pair<>(SamlProtocolConstants.PARAMETER_SAML_REQUEST, 
                    samlRequest));
            final String url = builder.buildURL();

            logger.debug("Built service callback url [{}]", url);
            return CommonUtils.constructServiceUrl(request, response,
                    url, this.serverName,
                    CasProtocolConstants.PARAMETER_SERVICE,
                    CasProtocolConstants.PARAMETER_TICKET, false);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    /**
     * Initiate authentication request.
     *
     * @param pair     the pair
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    protected void initiateAuthenticationRequest(final Pair<? extends SignableSAMLObject, MessageContext> pair,
                                                 final HttpServletResponse response,
                                                 final HttpServletRequest request) throws Exception {
                
        final AuthnRequest authnRequest = AuthnRequest.class.cast(pair.getFirst());
        final String issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest);
        final SamlRegisteredService registeredService = verifySamlRegisteredService(issuer);

        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
                SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver,
                        registeredService, authnRequest);

        final MessageContext ctx = pair.getSecond();
        if (!SAMLBindingSupport.isMessageSigned(ctx)) {
            if (adaptor.isAuthnRequestsSigned()) {
                logger.error("Metadata for [{}] says authentication requests are signed, yet this authentication request is not",
                        adaptor.getEntityId());
                throw new SAMLException("AuthN request is not signed but should be");
            }
            logger.info("Authentication request is not signed, so there is no need to verify its signature.");
        } else {
            this.samlObjectSigner.verifySamlProfileRequestIfNeeded(authnRequest, adaptor.getMetadataResolver(), request, ctx);
        }

        SamlUtils.logSamlObject(this.configBean, authnRequest);
        issueAuthenticationRequestRedirect(authnRequest, request, response);
    }

    public void setSamlObjectSigner(final SamlObjectSigner samlObjectSigner) {
        this.samlObjectSigner = samlObjectSigner;
    }

    public void setParserPool(final ParserPool parserPool) {
        this.parserPool = parserPool;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setWebApplicationServiceFactory(final ServiceFactory<WebApplicationService> webApplicationServiceFactory) {
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    public void setSamlRegisteredServiceCachingMetadataResolver(
            final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver) {
        this.samlRegisteredServiceCachingMetadataResolver = samlRegisteredServiceCachingMetadataResolver;
    }

    public void setConfigBean(final OpenSamlConfigBean configBean) {
        this.configBean = configBean;
    }

    public void setResponseBuilder(final SamlProfileSamlResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

    public Map<String, String> getAuthenticationContextClassMappings() {
        return authenticationContextClassMappings;
    }

    public void setAuthenticationContextClassMappings(final Map<String, String> authenticationContextClassMappings) {
        this.authenticationContextClassMappings = authenticationContextClassMappings;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getServerPrefix() {
        return serverPrefix;
    }

    public void setServerPrefix(final String serverPrefix) {
        this.serverPrefix = serverPrefix;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    public String getAuthenticationContextRequestParameter() {
        return authenticationContextRequestParameter;
    }

    public void setAuthenticationContextRequestParameter(final String authenticationContextRequestParameter) {
        this.authenticationContextRequestParameter = authenticationContextRequestParameter;
    }

    public boolean isSingleLogoutCallbacksDisabled() {
        return singleLogoutCallbacksDisabled;
    }

    public void setSingleLogoutCallbacksDisabled(final boolean singleLogoutCallbacksDisabled) {
        this.singleLogoutCallbacksDisabled = singleLogoutCallbacksDisabled;
    }

    public boolean isForceSignedLogoutRequests() {
        return forceSignedLogoutRequests;
    }

    public void setForceSignedLogoutRequests(final boolean forceSignedLogoutRequests) {
        this.forceSignedLogoutRequests = forceSignedLogoutRequests;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(final String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
}

