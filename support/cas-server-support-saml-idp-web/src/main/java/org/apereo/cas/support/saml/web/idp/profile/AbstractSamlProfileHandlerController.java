package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlIdPObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.support.WebUtils;

import com.google.common.base.Splitter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.BindingDescriptor;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSamlProfileHandlerController {
    /**
     * The Saml object signer.
     */
    protected final SamlIdPObjectSigner samlObjectSigner;

    /**
     * Authentication support to handle credentials and authn subsystem calls.
     */
    protected final AuthenticationSystemSupport authenticationSystemSupport;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * The Web application service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * The Saml registered service caching metadata resolver.
     */
    protected final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver;

    /**
     * The Config bean.
     */
    protected final OpenSamlConfigBean configBean;

    /**
     * The Response builder.
     */
    protected final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder;

    /**
     * The cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * Signature validator.
     */
    protected final SamlObjectSignatureValidator samlObjectSignatureValidator;

    /**
     * Callback service.
     */
    protected final Service callbackService;

    /**
     * Gets saml metadata adaptor for service.
     *
     * @param registeredService the registered service
     * @param authnRequest      the authn request
     * @return the saml metadata adaptor for service
     */
    protected Optional<SamlRegisteredServiceServiceProviderMetadataFacade> getSamlMetadataFacadeFor(final SamlRegisteredService registeredService,
                                                                                                    final RequestAbstractType authnRequest) {
        return SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, authnRequest);
    }

    /**
     * Gets saml metadata adaptor for service.
     *
     * @param registeredService the registered service
     * @param entityId          the entity id
     * @return the saml metadata adaptor for service
     */
    protected Optional<SamlRegisteredServiceServiceProviderMetadataFacade> getSamlMetadataFacadeFor(
        final SamlRegisteredService registeredService, final String entityId) {
        return SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, entityId);
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
        LOGGER.debug("Checking service access in CAS service registry for [{}]", serviceId);
        val registeredService =
            this.servicesManager.findServiceBy(this.webApplicationServiceFactory.createService(serviceId));
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("[{}] is not found in the registry or service access is denied. Ensure service is registered in service registry", serviceId);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }

        if (registeredService instanceof SamlRegisteredService) {
            val samlRegisteredService = (SamlRegisteredService) registeredService;
            LOGGER.debug("Located SAML service in the registry as [{}] with the metadata location of [{}]",
                samlRegisteredService.getServiceId(), samlRegisteredService.getMetadataLocation());
            return samlRegisteredService;
        }
        LOGGER.error("CAS has found a match for service [{}] in registry but the match is not defined as a SAML service", serviceId);
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
    }

    /**
     * Retrieve authn request authn request.
     *
     * @param request the request
     * @return the authn request
     * @throws Exception the exception
     */
    protected AuthnRequest retrieveSamlAuthenticationRequestFromHttpRequest(final HttpServletRequest request) throws Exception {
        LOGGER.debug("Retrieving authentication request from scope");
        val requestValue = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST);
        if (StringUtils.isBlank(requestValue)) {
            throw new IllegalArgumentException("SAML request could not be determined from the authentication request");
        }
        val encodedRequest = EncodingUtils.decodeBase64(requestValue.getBytes(StandardCharsets.UTF_8));
        val authnRequest = (AuthnRequest)
            XMLObjectSupport.unmarshallFromInputStream(this.configBean.getParserPool(), new ByteArrayInputStream(encodedRequest));
        return authnRequest;
    }

    /**
     * Build  cas assertion.
     *
     * @param authentication      the authentication
     * @param service             the service
     * @param registeredService   the registered service
     * @param attributesToCombine the attributes to combine
     * @return the assertion
     */
    protected Assertion buildCasAssertion(final Authentication authentication,
                                          final Service service,
                                          final RegisteredService registeredService,
                                          final Map<String, Object> attributesToCombine) {
        final Map attributes = registeredService.getAttributeReleasePolicy()
            .getAttributes(authentication.getPrincipal(), service, registeredService);
        val principal = new AttributePrincipalImpl(authentication.getPrincipal().getId(), attributes);
        val authnAttrs = new LinkedHashMap(authentication.getAttributes());
        authnAttrs.putAll(attributesToCombine);
        return new AssertionImpl(principal, DateTimeUtils.dateOf(authentication.getAuthenticationDate()),
            null, DateTimeUtils.dateOf(authentication.getAuthenticationDate()),
            authnAttrs);
    }

    /**
     * Build cas assertion.
     *
     * @param principal         the principal
     * @param registeredService the registered service
     * @param attributes        the attributes
     * @return the assertion
     */
    protected Assertion buildCasAssertion(final String principal,
                                          final RegisteredService registeredService,
                                          final Map<String, Object> attributes) {
        val p = new AttributePrincipalImpl(principal, attributes);
        return new AssertionImpl(p, DateTimeUtils.dateOf(ZonedDateTime.now()),
            null, DateTimeUtils.dateOf(ZonedDateTime.now()), attributes);
    }


    /**
     * Log cas validation assertion.
     *
     * @param assertion the assertion
     */
    protected void logCasValidationAssertion(final Assertion assertion) {
        LOGGER.debug("CAS Assertion Valid: [{}]", assertion.isValid());
        LOGGER.debug("CAS Assertion Principal: [{}]", assertion.getPrincipal().getName());
        LOGGER.debug("CAS Assertion authentication Date: [{}]", assertion.getAuthenticationDate());
        LOGGER.debug("CAS Assertion ValidFrom Date: [{}]", assertion.getValidFromDate());
        LOGGER.debug("CAS Assertion ValidUntil Date: [{}]", assertion.getValidUntilDate());
        LOGGER.debug("CAS Assertion Attributes: [{}]", assertion.getAttributes());
        LOGGER.debug("CAS Assertion Principal Attributes: [{}]", assertion.getPrincipal().getAttributes());
    }

    /**
     * Redirect request for authentication.
     *
     * @param pair     the pair
     * @param request  the request
     * @param response the response
     * @throws Exception the exception
     */
    protected void issueAuthenticationRequestRedirect(final Pair<? extends SignableSAMLObject, MessageContext> pair,
                                                      final HttpServletRequest request,
                                                      final HttpServletResponse response) throws Exception {
        val authnRequest = (AuthnRequest) pair.getLeft();
        val serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.debug("Created service url [{}]", DigestUtils.abbreviate(serviceUrl));

        val initialUrl = CommonUtils.constructRedirectUrl(casProperties.getServer().getLoginUrl(),
            CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
            authnRequest.isPassive());

        val urlToRedirectTo = buildRedirectUrlByRequestedAuthnContext(initialUrl, authnRequest, request);

        LOGGER.debug("Redirecting SAML authN request to [{}]", urlToRedirectTo);
        val authenticationRedirectStrategy = new DefaultAuthenticationRedirectStrategy();
        authenticationRedirectStrategy.redirect(request, response, urlToRedirectTo);
    }

    /**
     * Gets authentication context mappings.
     *
     * @return the authentication context mappings
     */
    protected Map<String, String> getAuthenticationContextMappings() {
        val mappings = new TreeMap<String, String>();
        casProperties.getAuthn().getSamlIdp().getAuthenticationContextClassMappings()
            .stream()
            .map(s -> {
                val bits = Splitter.on("->").splitToList(s);
                return Pair.of(bits.get(0), bits.get(1));
            })
            .forEach(p -> mappings.put(p.getKey(), p.getValue()));
        return mappings;
    }

    /**
     * Build redirect url by requested authn context.
     *
     * @param initialUrl   the initial url
     * @param authnRequest the authn request
     * @param request      the request
     * @return the redirect url
     */
    protected String buildRedirectUrlByRequestedAuthnContext(final String initialUrl, final AuthnRequest authnRequest, final HttpServletRequest request) {
        val authenticationContextClassMappings = this.casProperties.getAuthn().getSamlIdp().getAuthenticationContextClassMappings();
        if (authnRequest.getRequestedAuthnContext() == null || authenticationContextClassMappings == null || authenticationContextClassMappings.isEmpty()) {
            return initialUrl;
        }

        val mappings = getAuthenticationContextMappings();

        val p =
            authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs()
                .stream()
                .filter(ref -> {
                    val clazz = ref.getAuthnContextClassRef();
                    return mappings.containsKey(clazz);
                })
                .findFirst();

        if (p.isPresent()) {
            val mappedClazz = mappings.get(p.get().getAuthnContextClassRef());
            return initialUrl + '&' + casProperties.getAuthn().getMfa().getRequestParameter() + '=' + mappedClazz;
        }

        return initialUrl;
    }

    /**
     * Construct service url string.
     *
     * @param request  the request
     * @param response the response
     * @param pair     the pair
     * @return the string
     * @throws SamlException the saml exception
     */
    @SneakyThrows
    protected String constructServiceUrl(final HttpServletRequest request,
                                         final HttpServletResponse response,
                                         final Pair<? extends SignableSAMLObject, MessageContext> pair) throws SamlException {
        val authnRequest = (AuthnRequest) pair.getLeft();
        val messageContext = pair.getRight();

        try (val writer = SamlUtils.transformSamlObject(this.configBean, authnRequest)) {
            val builder = new URLBuilder(this.callbackService.getId());
            builder.getQueryParams().add(
                new net.shibboleth.utilities.java.support.collection.Pair<>(SamlProtocolConstants.PARAMETER_ENTITY_ID,
                    SamlIdPUtils.getIssuerFromSamlRequest(authnRequest)));

            val samlRequest = EncodingUtils.encodeBase64(writer.toString().getBytes(StandardCharsets.UTF_8));
            builder.getQueryParams().add(
                new net.shibboleth.utilities.java.support.collection.Pair<>(SamlProtocolConstants.PARAMETER_SAML_REQUEST,
                    samlRequest));
            builder.getQueryParams().add(
                new net.shibboleth.utilities.java.support.collection.Pair<>(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE,
                    SAMLBindingSupport.getRelayState(messageContext)));
            val url = builder.buildURL();

            LOGGER.trace("Built service callback url [{}]", url);
            return CommonUtils.constructServiceUrl(request, response,
                url, casProperties.getServer().getName(),
                CasProtocolConstants.PARAMETER_SERVICE,
                CasProtocolConstants.PARAMETER_TICKET, false);
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

        verifySamlAuthenticationRequest(pair, request);
        issueAuthenticationRequestRedirect(pair, request, response);
    }

    /**
     * Verify saml authentication request.
     *
     * @param authenticationContext the pair
     * @param request               the request
     * @return the pair
     * @throws Exception the exception
     */
    protected Pair<SamlRegisteredService, SamlRegisteredServiceServiceProviderMetadataFacade> verifySamlAuthenticationRequest(
        final Pair<? extends SignableSAMLObject, MessageContext> authenticationContext,
        final HttpServletRequest request) throws Exception {
        val authnRequest = (AuthnRequest) authenticationContext.getKey();
        val issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest);
        LOGGER.debug("Located issuer [{}] from authentication request", issuer);

        val registeredService = verifySamlRegisteredService(issuer);
        LOGGER.debug("Fetching saml metadata adaptor for [{}]", issuer);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(this.samlRegisteredServiceCachingMetadataResolver, registeredService, authnRequest);

        if (!adaptor.isPresent()) {
            LOGGER.warn("No metadata could be found for [{}]", issuer);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
        }

        val facade = adaptor.get();
        verifyAuthenticationContextSignature(authenticationContext, request, authnRequest, facade);
        SamlUtils.logSamlObject(this.configBean, authnRequest);
        return Pair.of(registeredService, facade);
    }

    /**
     * Verify authentication context signature.
     *
     * @param authenticationContext the authentication context
     * @param request               the request
     * @param authnRequest          the authn request
     * @param adaptor               the adaptor
     * @throws Exception the exception
     */
    protected void verifyAuthenticationContextSignature(final Pair<? extends SignableSAMLObject, MessageContext> authenticationContext,
                                                        final HttpServletRequest request, final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws Exception {
        val ctx = authenticationContext.getValue();
        verifyAuthenticationContextSignature(ctx, request, authnRequest, adaptor);
    }

    /**
     * Verify authentication context signature.
     *
     * @param ctx          the authentication context
     * @param request      the request
     * @param authnRequest the authn request
     * @param adaptor      the adaptor
     * @throws Exception the exception
     */
    protected void verifyAuthenticationContextSignature(final MessageContext ctx,
                                                        final HttpServletRequest request,
                                                        final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) throws Exception {
        if (!SAMLBindingSupport.isMessageSigned(ctx)) {
            LOGGER.debug("The authentication context is not signed");
            if (adaptor.isAuthnRequestsSigned()) {
                LOGGER.error("Metadata for [{}] says authentication requests are signed, yet authentication request is not", adaptor.getEntityId());
                throw new SAMLException("AuthN request is not signed but should be");
            }
            LOGGER.debug("Authentication request is not signed, so there is no need to verify its signature.");
        } else {
            LOGGER.debug("The authentication context is signed; Proceeding to validate signatures...");
            this.samlObjectSignatureValidator.verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, ctx);
        }
    }

    /**
     * Build saml response.
     *
     * @param response              the response
     * @param request               the request
     * @param authenticationContext the authentication context
     * @param casAssertion          the cas assertion
     * @param binding               the binding
     */
    protected void buildSamlResponse(final HttpServletResponse response,
                                     final HttpServletRequest request,
                                     final Pair<AuthnRequest, MessageContext> authenticationContext,
                                     final Assertion casAssertion,
                                     final String binding) {

        val authnRequest = authenticationContext.getKey();
        val pair = getRegisteredServiceAndFacade(authnRequest);

        val entityId = pair.getValue().getEntityId();
        LOGGER.debug("Preparing SAML response for [{}]", entityId);
        this.responseBuilder.build(authnRequest, request, response, casAssertion,
            pair.getKey(), pair.getValue(), binding, authenticationContext.getValue());
        LOGGER.info("Built the SAML response for [{}]", entityId);
    }

    /**
     * Gets registered service and facade.
     *
     * @param request the request
     * @return the registered service and facade
     */
    protected Pair<SamlRegisteredService, SamlRegisteredServiceServiceProviderMetadataFacade> getRegisteredServiceAndFacade(final AuthnRequest request) {
        val issuer = SamlIdPUtils.getIssuerFromSamlRequest(request);
        LOGGER.debug("Located issuer [{}] from authentication context", issuer);

        val registeredService = verifySamlRegisteredService(issuer);

        LOGGER.debug("Located SAML metadata for [{}]", registeredService.getServiceId());
        val adaptor =
            getSamlMetadataFacadeFor(registeredService, request);

        if (!adaptor.isPresent()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                "Cannot find metadata linked to " + issuer);
        }
        val facade = adaptor.get();
        return Pair.of(registeredService, facade);
    }


    /**
     * Decode soap 11 context.
     *
     * @param request the request
     * @return the soap 11 context
     */
    protected MessageContext decodeSoapRequest(final HttpServletRequest request) {
        try {
            val decoder = new HTTPSOAP11Decoder();
            decoder.setParserPool(this.configBean.getParserPool());
            decoder.setHttpServletRequest(request);

            val binding = new BindingDescriptor();
            binding.setId(getClass().getName());
            binding.setShortName(getClass().getName());
            binding.setSignatureCapable(true);
            binding.setSynchronous(true);

            decoder.setBindingDescriptor(binding);
            decoder.initialize();
            decoder.decode();
            return decoder.getMessageContext();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Handle unauthorized service exception.
     *
     * @param req the req
     * @param ex  the ex
     * @return the model and view
     */
    @ExceptionHandler(UnauthorizedServiceException.class)
    public ModelAndView handleUnauthorizedServiceException(final HttpServletRequest req, final Exception ex) {
        return WebUtils.produceUnauthorizedErrorView();
    }
}

