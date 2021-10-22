package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPCoreProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.support.WebUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.net.URLBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.servlet.BaseHttpServletRequestXMLMessageDecoder;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.binding.BindingDescriptor;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
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
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractSamlIdPProfileHandlerController {
    /**
     * SAML profile configuration context.
     */
    protected final SamlProfileHandlerConfigurationContext configurationContext;

    /**
     * Log cas validation assertion.
     *
     * @param assertion the assertion
     */
    protected static void logCasValidationAssertion(final Assertion assertion) {
        LOGGER.debug("CAS Assertion Valid: [{}]", assertion.isValid());
        LOGGER.debug("CAS Assertion Principal: [{}]", assertion.getPrincipal().getName());
        LOGGER.debug("CAS Assertion authentication Date: [{}]", assertion.getAuthenticationDate());
        LOGGER.debug("CAS Assertion ValidFrom Date: [{}]", assertion.getValidFromDate());
        LOGGER.debug("CAS Assertion ValidUntil Date: [{}]", assertion.getValidUntilDate());
        LOGGER.debug("CAS Assertion Attributes: [{}]", assertion.getAttributes());
        LOGGER.debug("CAS Assertion Principal Attributes: [{}]", assertion.getPrincipal().getAttributes());
    }

    /**
     * Bind relay state parameter.
     *
     * @param request      the request
     * @param response     the response
     * @param authnContext the authn context
     * @param relayState   the relay state
     * @return the message context
     */
    protected static MessageContext bindRelayStateParameter(final HttpServletRequest request,
                                                            final HttpServletResponse response,
                                                            final Pair<? extends RequestAbstractType, MessageContext> authnContext,
                                                            final String relayState) {
        val messageContext = authnContext.getValue();
        LOGGER.trace("Relay state is [{}]", relayState);
        SAMLBindingSupport.setRelayState(messageContext, relayState);
        return messageContext;
    }

    /**
     * Handle unauthorized service exception.
     *
     * @param req the req
     * @param ex  the ex
     * @return the model and view
     */
    @ExceptionHandler({PrincipalException.class, UnauthorizedServiceException.class, SamlException.class})
    public ModelAndView handleUnauthorizedServiceException(final HttpServletRequest req, final Exception ex) {
        return WebUtils.produceUnauthorizedErrorView(ex);
    }

    /**
     * Gets saml metadata adaptor for service.
     *
     * @param registeredService the registered service
     * @param authnRequest      the authn request
     * @return the saml metadata adaptor for service
     */
    protected Optional<SamlRegisteredServiceServiceProviderMetadataFacade> getSamlMetadataFacadeFor(
        final SamlRegisteredService registeredService,
        final RequestAbstractType authnRequest) {
        return SamlRegisteredServiceServiceProviderMetadataFacade.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, authnRequest);
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
        return SamlRegisteredServiceServiceProviderMetadataFacade.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, entityId);
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
        val service = configurationContext.getWebApplicationServiceFactory().createService(serviceId);
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(serviceId));
        LOGGER.debug("Checking service access in CAS service registry for [{}]", service);
        val registeredService = configurationContext.getServicesManager().findServiceBy(service, SamlRegisteredService.class);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("[{}] is not found in the registry or service access is denied.", serviceId);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        LOGGER.debug("Located SAML service in the registry as [{}] with the metadata location of [{}]",
            registeredService.getServiceId(), registeredService.getMetadataLocation());
        return registeredService;
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
    protected AuthenticatedAssertionContext buildCasAssertion(final Authentication authentication,
                                                              final Service service,
                                                              final RegisteredService registeredService,
                                                              final Map<String, List<Object>> attributesToCombine) {
        val attributes = registeredService.getAttributeReleasePolicy()
            .getAttributes(authentication.getPrincipal(), service, registeredService);
        val principalId = registeredService.getUsernameAttributeProvider()
            .resolveUsername(authentication.getPrincipal(), service, registeredService);
        attributes.putAll(attributesToCombine);

        val authnAttributes = configurationContext.getAuthenticationAttributeReleasePolicy()
            .getAuthenticationAttributesForRelease(authentication, null, Map.of(), registeredService);

        return AuthenticatedAssertionContext.builder()
            .name(principalId)
            .authenticationDate(DateTimeUtils.zonedDateTimeOf(authentication.getAuthenticationDate()))
            .validFromDate(DateTimeUtils.zonedDateTimeOf(authentication.getAuthenticationDate()))
            .attributes(CollectionUtils.merge(attributes, authnAttributes))
            .build();
    }

    /**
     * Build cas assertion.
     *
     * @param principal         the principal
     * @param registeredService the registered service
     * @param attributes        the attributes
     * @return the assertion
     */
    protected AuthenticatedAssertionContext buildCasAssertion(final String principal,
                                                              final RegisteredService registeredService,
                                                              final Map<String, Object> attributes) {
        return AuthenticatedAssertionContext.builder()
            .name(principal)
            .attributes(attributes)
            .build();
    }

    /**
     * Redirect request for authentication.
     *
     * @param pair     the pair
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    protected ModelAndView issueAuthenticationRequestRedirect(
        final Pair<? extends SignableSAMLObject, MessageContext> pair,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        val authnRequest = (AuthnRequest) pair.getLeft();
        val serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.debug("Created service url [{}]", DigestUtils.abbreviate(serviceUrl));

        val properties = configurationContext.getCasProperties();
        val urlToRedirectTo = CommonUtils.constructRedirectUrl(properties.getServer().getLoginUrl(),
            CasProtocolConstants.PARAMETER_SERVICE, serviceUrl, authnRequest.isForceAuthn(),
            authnRequest.isPassive());
        LOGGER.debug("Redirecting SAML authN request to [{}]", urlToRedirectTo);

        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type == SamlIdPCoreProperties.SessionStorageTypes.BROWSER_SESSION_STORAGE) {
            val context = new JEEContext(request, response);
            val sessionStorage = configurationContext.getSessionStore()
                .getTrackableSession(context).map(BrowserSessionStorage.class::cast)
                .orElseThrow(() -> new IllegalStateException("Unable to determine trackable session for storage"));
            sessionStorage.setDestinationUrl(urlToRedirectTo);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_SESSION_STORAGE_WRITE,
                BrowserSessionStorage.KEY_SESSION_STORAGE, sessionStorage);
        }
        LOGGER.debug("Redirecting SAML authN request to [{}]", urlToRedirectTo);
        val mv = new ModelAndView(new RedirectView(urlToRedirectTo));
        mv.setStatus(HttpStatus.FOUND);
        return mv;
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
        val builder = new URLBuilder(configurationContext.getCallbackService().getId());

        builder.getQueryParams().add(
            new net.shibboleth.utilities.java.support.collection.Pair<>(SamlProtocolConstants.PARAMETER_ENTITY_ID,
                SamlIdPUtils.getIssuerFromSamlObject(authnRequest)));
        storeAuthenticationRequest(request, response, pair);
        val url = builder.buildURL();

        LOGGER.trace("Built service callback url [{}]", url);
        return CommonUtils.constructServiceUrl(request, response,
            url, configurationContext.getCasProperties().getServer().getName(),
            CasProtocolConstants.PARAMETER_SERVICE,
            CasProtocolConstants.PARAMETER_TICKET, false);
    }

    /**
     * Initiate authentication request.
     *
     * @param pair     the pair
     * @param response the response
     * @param request  the request
     * @return the model and view
     * @throws Exception the exception
     */
    protected ModelAndView initiateAuthenticationRequest(final Pair<? extends RequestAbstractType, MessageContext> pair,
                                                         final HttpServletResponse response,
                                                         final HttpServletRequest request) throws Exception {
        autoConfigureCookiePath(request);
        verifySamlAuthenticationRequest(pair, request);
        val sso = singleSignOnSessionExists(pair, request, response);
        if (sso.isEmpty()) {
            return issueAuthenticationRequestRedirect(pair, request, response);
        }
        buildResponseBasedSingleSignOnSession(pair, sso.get(), request, response);
        return null;
    }

    /**
     * Build response based single sign on session.
     * The http response before encoding the SAML response is reset
     * to ensure a clean slate from previous attempts, specially
     * when requests/responses are produced rapidly.
     *
     * @param context              the pair
     * @param ticketGrantingTicket the authentication
     * @param request              the request
     * @param response             the response
     */
    protected void buildResponseBasedSingleSignOnSession(
        final Pair<? extends RequestAbstractType, MessageContext> context,
        final TicketGrantingTicket ticketGrantingTicket,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        val authnRequest = (AuthnRequest) context.getLeft();
        val id = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        val service = configurationContext.getWebApplicationServiceFactory().createService(id);
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(id));
        val registeredService = configurationContext.getServicesManager().findServiceBy(service, SamlRegisteredService.class);

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(ticketGrantingTicket.getAuthentication())
            .registeredService(registeredService)
            .httpRequest(request)
            .httpResponse(response)
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        val assertion = buildCasAssertion(ticketGrantingTicket.getAuthentication(), service, registeredService, Map.of());
        val authenticationContext = buildAuthenticationContextPair(request, response, context);
        val binding = determineProfileBinding(authenticationContext);

        val messageContext = authenticationContext.getRight();
        val relayState = SAMLBindingSupport.getRelayState(messageContext);
        SAMLBindingSupport.setRelayState(authenticationContext.getRight(), relayState);
        response.reset();

        val factory = (ServiceTicketFactory) getConfigurationContext().getTicketFactory().get(ServiceTicket.class);
        val st = factory.create(ticketGrantingTicket, service, false, ServiceTicket.class);
        getConfigurationContext().getTicketRegistry().addTicket(st);
        getConfigurationContext().getTicketRegistry().updateTicket(ticketGrantingTicket);
        buildSamlResponse(response, request, authenticationContext, assertion, binding);
    }

    /**
     * Build authentication context pair pair.
     *
     * @param request      the request
     * @param response     the response
     * @param authnContext the authn context
     * @return the pair
     */
    protected Pair<? extends RequestAbstractType, MessageContext> buildAuthenticationContextPair(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Pair<? extends RequestAbstractType, MessageContext> authnContext) {
        val relayState = Optional.ofNullable(SAMLBindingSupport.getRelayState(authnContext.getValue()))
            .orElseGet(() -> request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
        val messageContext = bindRelayStateParameter(request, response, authnContext, relayState);
        return Pair.of(authnContext.getLeft(), messageContext);
    }

    /**
     * Single sign on session exists.
     *
     * @param pair     the pair
     * @param request  the request
     * @param response the response
     * @return the boolean
     */
    protected Optional<TicketGrantingTicket> singleSignOnSessionExists(
        final Pair<? extends SignableSAMLObject, MessageContext> pair,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        val authnRequest = AuthnRequest.class.cast(pair.getLeft());
        if (authnRequest.isForceAuthn()) {
            LOGGER.trace("Authentication request asks for forced authn. Ignoring existing single sign-on session, if any");
            return Optional.empty();
        }
        val cookie = configurationContext.getTicketGrantingTicketCookieGenerator().retrieveCookieValue(request);
        if (StringUtils.isBlank(cookie)) {
            LOGGER.trace("Single sign-on session cannot be found or determined. Ignoring single sign-on session");
            return Optional.empty();
        }

        val ticketGrantingTicket = configurationContext.getTicketRegistrySupport().getTicketGrantingTicket(cookie);
        if (ticketGrantingTicket == null) {
            LOGGER.debug("Authentication transaction linked to single sign-on session cannot determined.");
            return Optional.empty();
        }

        val authn = ticketGrantingTicket.getAuthentication();
        LOGGER.debug("Located single sign-on authentication for principal [{}]", authn.getPrincipal());
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        val service = configurationContext.getWebApplicationServiceFactory().createService(issuer);
        val registeredService = configurationContext.getServicesManager().findServiceBy(service);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .build()
            .attribute(Service.class.getName(), service)
            .attribute(RegisteredService.class.getName(), registeredService)
            .attribute(Issuer.class.getName(), issuer)
            .attribute(Authentication.class.getName(), authn)
            .attribute(TicketGrantingTicket.class.getName(), cookie)
            .attribute(AuthnRequest.class.getName(), authnRequest);
        val ssoStrategy = configurationContext.getSingleSignOnParticipationStrategy();
        LOGGER.debug("Checking for single sign-on participation for issuer [{}]", issuer);
        val ssoAvailable = ssoStrategy.supports(ssoRequest) && ssoStrategy.isParticipating(ssoRequest);
        return ssoAvailable ? Optional.of(ticketGrantingTicket) : Optional.empty();
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
        final Pair<? extends RequestAbstractType, MessageContext> authenticationContext,
        final HttpServletRequest request) throws Exception {
        val authnRequest = (AuthnRequest) authenticationContext.getKey();
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        LOGGER.debug("Located issuer [{}] from authentication request", issuer);

        val registeredService = verifySamlRegisteredService(issuer);
        LOGGER.debug("Fetching saml metadata adaptor for [{}]", issuer);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, authnRequest);

        if (adaptor.isEmpty()) {
            LOGGER.warn("No metadata could be found for [{}]", issuer);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + issuer);
        }

        val facade = adaptor.get();
        verifyAuthenticationContextSignature(authenticationContext, request, authnRequest, facade, registeredService);
        val binding = determineProfileBinding(authenticationContext);
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, authenticationContext.getRight()), facade, binding);
        LOGGER.debug("Determined SAML2 endpoint for authentication request as [{}]",
            StringUtils.defaultIfBlank(acs.getResponseLocation(), acs.getLocation()));

        SamlUtils.logSamlObject(configurationContext.getOpenSamlConfigBean(), authnRequest);
        return Pair.of(registeredService, facade);
    }

    /**
     * Verify authentication context signature.
     *
     * @param authenticationContext the authentication context
     * @param request               the request
     * @param authnRequest          the authn request
     * @param adaptor               the adaptor
     * @param registeredService     the registered service
     * @throws Exception the exception
     */
    protected void verifyAuthenticationContextSignature(final Pair<? extends SignableSAMLObject, MessageContext> authenticationContext,
                                                        final HttpServletRequest request, final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                        final SamlRegisteredService registeredService) throws Exception {
        val ctx = authenticationContext.getValue();
        verifyAuthenticationContextSignature(ctx, request, authnRequest, adaptor, registeredService);
    }

    /**
     * Verify authentication context signature.
     *
     * @param ctx               the authentication context
     * @param request           the request
     * @param authnRequest      the authn request
     * @param adaptor           the adaptor
     * @param registeredService the registered service
     * @throws Exception the exception
     */
    protected void verifyAuthenticationContextSignature(final MessageContext ctx,
                                                        final HttpServletRequest request,
                                                        final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                        final SamlRegisteredService registeredService) throws Exception {
        if (!SAMLBindingSupport.isMessageSigned(ctx)) {
            LOGGER.trace("The authentication context is not signed");
            if (adaptor.isAuthnRequestsSigned() && !registeredService.isSkipValidatingAuthnRequest()) {
                LOGGER.error("Metadata for [{}] says authentication requests are signed, yet request is not", adaptor.getEntityId());
                throw new SAMLException("Request is not signed but should be");
            }
            LOGGER.trace("Request is not signed or validation is skipped, so there is no need to verify its signature.");
        } else if (!registeredService.isSkipValidatingAuthnRequest()) {
            LOGGER.trace("The authentication context is signed; Proceeding to validate signatures...");
            configurationContext.getSamlObjectSignatureValidator().verifySamlProfileRequestIfNeeded(authnRequest, adaptor, request, ctx);
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
                                     final Pair<? extends RequestAbstractType, MessageContext> authenticationContext,
                                     final AuthenticatedAssertionContext casAssertion,
                                     final String binding) {

        val authnRequest = AuthnRequest.class.cast(authenticationContext.getKey());
        val pair = getRegisteredServiceAndFacade(authnRequest);

        val entityId = pair.getValue().getEntityId();
        LOGGER.debug("Preparing SAML response for [{}]", entityId);
        configurationContext.getResponseBuilder().build(authnRequest, request, response, casAssertion,
            pair.getKey(), pair.getValue(), binding, authenticationContext.getValue());
        LOGGER.info("Built the SAML response for [{}]", entityId);
    }

    /**
     * Gets registered service and facade.
     *
     * @param request the request
     * @return the registered service and facade
     */
    protected Pair<SamlRegisteredService, SamlRegisteredServiceServiceProviderMetadataFacade> getRegisteredServiceAndFacade(
        final AuthnRequest request) {
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(request);
        LOGGER.debug("Located issuer [{}] from authentication context", issuer);

        val registeredService = verifySamlRegisteredService(issuer);

        LOGGER.debug("Located SAML metadata for [{}]", registeredService.getServiceId());
        val adaptor = getSamlMetadataFacadeFor(registeredService, request);

        if (adaptor.isEmpty()) {
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
            decoder.setParserPool(configurationContext.getOpenSamlConfigBean().getParserPool());
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
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Auto configure cookie path.
     *
     * @param request the request
     */
    protected void autoConfigureCookiePath(final HttpServletRequest request) {
        val casProperties = configurationContext.getCasProperties();
        val sessionStorageType = casProperties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (sessionStorageType == SamlIdPCoreProperties.SessionStorageTypes.TICKET_REGISTRY
            && casProperties.getSessionReplication().getCookie().isAutoConfigureCookiePath()) {

            val contextPath = request.getContextPath();
            val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

            val cookieBuilder = configurationContext.getSamlDistributedSessionCookieGenerator();
            val path = cookieBuilder.getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for SAML2 distributed session cookie generator to: [{}]", cookiePath);
                cookieBuilder.setCookiePath(cookiePath);
            } else {
                LOGGER.trace("SAML2 authentication cookie domain is [{}] with path [{}]",
                    cookieBuilder.getCookieDomain(), path);
            }
        }
    }

    /**
     * Handle profile request.
     *
     * @param response the response
     * @param request  the request
     * @param decoder  the decoder
     * @return the model and view
     */
    protected ModelAndView handleSsoPostProfileRequest(final HttpServletResponse response,
                                                       final HttpServletRequest request,
                                                       final BaseHttpServletRequestXMLMessageDecoder decoder) {
        try {
            val result = getConfigurationContext().getSamlHttpRequestExtractor()
                .extract(request, decoder, AuthnRequest.class)
                .orElseThrow(() -> new IllegalArgumentException("Unable to extract SAML request"));
            val context = Pair.of(AuthnRequest.class.cast(result.getLeft()), result.getRight());
            return initiateAuthenticationRequest(context, response, request);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return WebUtils.produceErrorView(e);
        }
    }

    /**
     * Retrieve authentication request.
     *
     * @param response the response
     * @param request  the request
     * @return the authn request
     */
    @Synchronized
    protected final Pair<? extends RequestAbstractType, MessageContext> retrieveAuthenticationRequest(final HttpServletResponse response,
                                                                                                      final HttpServletRequest request) {
        LOGGER.info("Received SAML callback profile request [{}]", request.getRequestURI());
        val webContext = new JEEContext(request, response);
        return SamlIdPUtils.retrieveSamlRequest(webContext, configurationContext.getSessionStore(),
                configurationContext.getOpenSamlConfigBean(), AuthnRequest.class)
            .orElseThrow(() -> new IllegalArgumentException("SAML request or context could not be determined from session store"));
    }

    /**
     * Store authentication request.
     *
     * @param request  the request
     * @param response the response
     * @param context  the pair
     * @throws Exception the exception
     */
    @Synchronized
    protected void storeAuthenticationRequest(final HttpServletRequest request, final HttpServletResponse response,
                                              final Pair<? extends SignableSAMLObject, MessageContext> context) throws Exception {
        val webContext = new JEEContext(request, response);
        SamlIdPUtils.storeSamlRequest(webContext, configurationContext.getOpenSamlConfigBean(),
            configurationContext.getSessionStore(), context);
    }

    /**
     * Determine profile binding.
     *
     * @param authenticationContext the authentication context
     * @return the string
     */
    protected String determineProfileBinding(final Pair<? extends RequestAbstractType, MessageContext> authenticationContext) {
        val authnRequest = AuthnRequest.class.cast(authenticationContext.getKey());
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

