package org.apereo.cas.support.saml.web.idp.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.idp.MissingSamlAuthnRequestException;
import org.apereo.cas.support.saml.idp.SamlIdPSessionManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.AuthenticatedAssertionContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.validation.TicketValidationResult;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.opensaml.core.xml.XMLObject;
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
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final CasReentrantLock lock = new CasReentrantLock();

    protected static void logCasValidationAssertion(final TicketValidationResult assertion) {
        LOGGER.debug("CAS Assertion Principal: [{}]", assertion.getPrincipal());
        LOGGER.debug("CAS Assertion Authentication Attributes: [{}]", assertion.getAttributes());
        LOGGER.debug("CAS Assertion Service: [{}]", assertion.getService());
    }

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
     * Handle SAML2 exceptions.
     *
     * @param req the req
     * @param ex  the ex
     * @return the model and view
     */
    @ExceptionHandler({PrincipalException.class, UnauthorizedServiceException.class, SamlException.class})
    protected ModelAndView handleUnauthorizedServiceException(final HttpServletRequest req, final Exception ex) {
        return WebUtils.produceUnauthorizedErrorView(ex);
    }

    @ExceptionHandler(MissingSamlAuthnRequestException.class)
    protected ModelAndView handleMissingAuthnRequest(final HttpServletRequest req, final Exception ex) {
        return WebUtils.produceErrorView(SamlIdPConstants.VIEW_ID_SAML_IDP_ERROR, ex);
    }

    protected Optional<SamlRegisteredServiceMetadataAdaptor> getSamlMetadataFacadeFor(
        final SamlRegisteredService registeredService,
        final RequestAbstractType authnRequest) {
        return SamlRegisteredServiceMetadataAdaptor.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, authnRequest);
    }

    protected Optional<SamlRegisteredServiceMetadataAdaptor> getSamlMetadataFacadeFor(
        final SamlRegisteredService registeredService, final String entityId) {
        return SamlRegisteredServiceMetadataAdaptor.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, entityId);
    }

    protected SamlRegisteredService verifySamlRegisteredService(final String serviceId,
                                                                final HttpServletRequest request) {
        if (StringUtils.isBlank(serviceId)) {
            throw UnauthorizedServiceException.denied("Could not verify/locate SAML registered service since no serviceId is provided");
        }
        val service = configurationContext.getWebApplicationServiceFactory().createService(serviceId, request);
        service.getAttributes().put(SamlProtocolConstants.PARAMETER_ENTITY_ID, CollectionUtils.wrapList(serviceId));
        LOGGER.debug("Checking service access in CAS service registry for [{}]", service);
        val registeredService = configurationContext.getServicesManager().findServiceBy(service, SamlRegisteredService.class);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            LOGGER.warn("[{}] is not found in the registry or service access is denied.", serviceId);
            throw UnauthorizedServiceException.denied("Rejected: %s".formatted(serviceId));
        }
        LOGGER.debug("Located SAML service in the registry as [{}] with the metadata location of [{}]",
            registeredService.getServiceId(), registeredService.getMetadataLocation());
        return registeredService;
    }

    protected AuthenticatedAssertionContext buildCasAssertion(final Authentication authentication,
                                                              final Service service,
                                                              final RegisteredService registeredService,
                                                              final Map<String, List<Object>> attributesToCombine) throws Throwable {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(getConfigurationContext().getOpenSamlConfigBean().getApplicationContext())
            .service(service)
            .principal(authentication.getPrincipal())
            .build();
        val attributes = registeredService.getAttributeReleasePolicy().getAttributes(context);

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(authentication.getPrincipal())
            .applicationContext(getConfigurationContext().getOpenSamlConfigBean().getApplicationContext())
            .build();
        val principalId = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);
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

    protected AuthenticatedAssertionContext buildCasAssertion(final String principal,
                                                              final RegisteredService registeredService,
                                                              final Map<String, Object> attributes) {
        return AuthenticatedAssertionContext.builder()
            .name(principal)
            .attributes(attributes)
            .build();
    }

    protected ModelAndView issueAuthenticationRequestRedirect(
        final Pair<? extends SignableSAMLObject, MessageContext> pair,
        final HttpServletRequest request,
        final HttpServletResponse response) throws Exception {
        val authnRequest = (AuthnRequest) pair.getLeft();
        val serviceUrl = constructServiceUrl(request, response, pair);
        LOGGER.debug("Created service url [{}]", DigestUtils.abbreviate(serviceUrl));

        val properties = configurationContext.getCasProperties();
        val urlToRedirectTo = constructRedirectUrl(
            serviceUrl,
            Map.of(
                SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, StringUtils.defaultString(fetchRelayState(request, pair)),
                CasProtocolConstants.PARAMETER_RENEW, BooleanUtils.toString(BooleanUtils.toBoolean(authnRequest.isForceAuthn()), "true", StringUtils.EMPTY),
                CasProtocolConstants.PARAMETER_GATEWAY, BooleanUtils.toString(BooleanUtils.toBoolean(authnRequest.isPassive()), "true", StringUtils.EMPTY)
            )
        );
        LOGGER.debug("Redirecting SAML authentication request to [{}]", urlToRedirectTo);

        val type = properties.getAuthn().getSamlIdp().getCore().getSessionStorageType();
        if (type.isBrowserStorage()) {
            val context = new JEEContext(request, response);
            val sessionStorage = configurationContext.getSessionStore()
                .getTrackableSession(context).map(BrowserStorage.class::cast)
                .orElseThrow(() -> new IllegalStateException("Unable to determine trackable session for storage"));
            sessionStorage.setDestinationUrl(urlToRedirectTo);
            return new ModelAndView(CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_WRITE,
                BrowserStorage.PARAMETER_BROWSER_STORAGE, sessionStorage);
        }
        LOGGER.debug("Redirecting SAML authN request to [{}]", urlToRedirectTo);
        val mv = new ModelAndView(new RedirectView(urlToRedirectTo));
        mv.setStatus(HttpStatus.FOUND);
        return mv;
    }

    private String constructRedirectUrl(final String serviceUrl, final Map<String, String> parameters) throws Exception {
        val properties = configurationContext.getCasProperties();
        val urlBuilder = new URIBuilder(properties.getServer().getLoginUrl());
        urlBuilder.addParameter(CasProtocolConstants.PARAMETER_SERVICE, serviceUrl);
        parameters
            .entrySet()
            .stream()
            .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
            .forEach(entry -> urlBuilder.addParameter(entry.getKey(), EncodingUtils.urlEncode(entry.getValue())));
        return urlBuilder.build().toASCIIString();
    }

    protected String constructServiceUrl(final HttpServletRequest request, final HttpServletResponse response,
                                         final Pair<? extends SignableSAMLObject, MessageContext> pair) throws Exception {
        val authnRequest = (AuthnRequest) pair.getLeft();
        val builder = new URIBuilder(configurationContext.getCallbackService().getId());
        builder.addParameter(SamlIdPConstants.AUTHN_REQUEST_ID, authnRequest.getID());
        builder.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, SamlIdPUtils.getIssuerFromSamlObject(authnRequest));
        storeAuthenticationRequest(request, response, pair);
        val url = builder.build().toURL().toExternalForm();
        LOGGER.trace("Built service callback url [{}]", url);
        return url;
    }

    protected ModelAndView initiateAuthenticationRequest(final Pair<? extends RequestAbstractType, MessageContext> pair,
                                                         final HttpServletResponse response,
                                                         final HttpServletRequest request) throws Throwable {
        autoConfigureCookiePath(request);
        verifySamlAuthenticationRequest(pair, request);
        val sso = singleSignOnSessionExists(pair, request, response);
        if (sso.isEmpty()) {
            return issueAuthenticationRequestRedirect(pair, request, response);
        }
        buildResponseBasedSingleSignOnSession(pair, sso.get(), request, response);
        return null;
    }

    protected void buildResponseBasedSingleSignOnSession(
        final Pair<? extends RequestAbstractType, MessageContext> context,
        final TicketGrantingTicket ticketGrantingTicket,
        final HttpServletRequest request,
        final HttpServletResponse response) throws Throwable {
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
        val binding = determineProfileBinding(authenticationContext, request);

        val messageContext = authenticationContext.getRight();
        val relayState = SAMLBindingSupport.getRelayState(messageContext);
        SAMLBindingSupport.setRelayState(authenticationContext.getRight(), relayState);
        response.reset();

        val factory = (ServiceTicketFactory) getConfigurationContext().getTicketFactory().get(ServiceTicket.class);
        val st = factory.create(ticketGrantingTicket, service, false, ServiceTicket.class);
        getConfigurationContext().getTicketRegistry().addTicket(st);
        getConfigurationContext().getTicketRegistry().updateTicket(ticketGrantingTicket);
        buildSamlResponse(response, request, authenticationContext, Optional.of(assertion), binding, st.getId());
    }

    protected XMLObject buildSamlResponse(final HttpServletResponse response,
                                          final HttpServletRequest request,
                                          final Pair<? extends RequestAbstractType, MessageContext> authenticationContext,
                                          final Optional<AuthenticatedAssertionContext> casAssertion,
                                          final String binding,
                                          final String sessionIndex) throws Exception {
        val authnRequest = (AuthnRequest) authenticationContext.getKey();
        val pair = getRegisteredServiceAndFacade(authnRequest, request);

        val entityId = pair.getValue().getEntityId();
        LOGGER.debug("Preparing SAML2 response for [{}]", entityId);
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(authnRequest)
            .httpRequest(request)
            .httpResponse(response)
            .authenticatedAssertion(casAssertion)
            .registeredService(pair.getKey())
            .adaptor(pair.getValue())
            .binding(binding)
            .messageContext(authenticationContext.getValue())
            .sessionIndex(sessionIndex)
            .build();
        val samlResponse = configurationContext.getResponseBuilder().build(buildContext);
        LOGGER.info("Built the SAML2 response for [{}]", entityId);
        return samlResponse;
    }

    protected Pair<? extends RequestAbstractType, MessageContext> buildAuthenticationContextPair(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Pair<? extends RequestAbstractType, MessageContext> authnContext) {
        val relayState = fetchRelayState(request, authnContext);
        val messageContext = bindRelayStateParameter(request, response, authnContext, relayState);
        return Pair.of(authnContext.getLeft(), messageContext);
    }

    private static String fetchRelayState(final HttpServletRequest request,
                                          final Pair<? extends SignableSAMLObject, MessageContext> authnContext) {
        return Optional.ofNullable(SAMLBindingSupport.getRelayState(authnContext.getValue()))
            .orElseGet(() -> request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE));
    }

    protected Optional<TicketGrantingTicket> singleSignOnSessionExists(
        final Pair<? extends SignableSAMLObject, MessageContext> pair,
        final HttpServletRequest request,
        final HttpServletResponse response) throws Throwable {
        val authnRequest = (AuthnRequest) pair.getLeft();
        if (Boolean.TRUE.equals(authnRequest.isForceAuthn())) {
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

        val authentication = ticketGrantingTicket.getAuthentication();
        LOGGER.debug("Located single sign-on authentication for principal [{}]", authentication.getPrincipal());
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        val service = configurationContext.getWebApplicationServiceFactory().createService(issuer);
        val registeredService = configurationContext.getServicesManager().findServiceBy(service);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .httpServletResponse(response)
            .build()
            .attribute(Service.class.getName(), service)
            .attribute(RegisteredService.class.getName(), registeredService)
            .attribute(Issuer.class.getName(), issuer)
            .attribute(Authentication.class.getName(), authentication)
            .attribute(TicketGrantingTicket.class.getName(), cookie)
            .attribute(AuthnRequest.class.getName(), authnRequest);
        val ssoStrategy = configurationContext.getSingleSignOnParticipationStrategy();
        LOGGER.debug("Checking for single sign-on participation for issuer [{}]", issuer);
        val ssoAvailable = ssoStrategy.supports(ssoRequest) && ssoStrategy.isParticipating(ssoRequest);
        return ssoAvailable ? Optional.of(ticketGrantingTicket) : Optional.empty();
    }

    protected Pair<SamlRegisteredService, SamlRegisteredServiceMetadataAdaptor> verifySamlAuthenticationRequest(
        final Pair<? extends RequestAbstractType, MessageContext> authenticationContext,
        final HttpServletRequest request) throws Throwable {
        val authnRequest = (AuthnRequest) authenticationContext.getKey();
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        LOGGER.debug("Located issuer [{}] from authentication request", issuer);

        val registeredService = verifySamlRegisteredService(issuer, request);
        LOGGER.debug("Fetching SAML2 metadata adaptor for [{}]", issuer);
        val adaptor = SamlRegisteredServiceMetadataAdaptor.get(
            configurationContext.getSamlRegisteredServiceCachingMetadataResolver(), registeredService, authnRequest);

        if (adaptor.isEmpty()) {
            LOGGER.warn("No metadata could be found for [{}]", issuer);
            throw UnauthorizedServiceException.denied("Cannot find metadata linked to %s".formatted(issuer));
        }

        val facade = adaptor.get();
        verifyAuthenticationContextSignature(authenticationContext, request, authnRequest, facade, registeredService);
        val binding = determineProfileBinding(authenticationContext, request);
        val acs = SamlIdPUtils.determineEndpointForRequest(Pair.of(authnRequest, authenticationContext.getRight()), facade, binding);
        LOGGER.debug("Determined SAML2 endpoint for authentication request as [{}]",
            StringUtils.defaultIfBlank(acs.getResponseLocation(), acs.getLocation()));

        configurationContext.getOpenSamlConfigBean().logObject(authnRequest);
        return Pair.of(registeredService, facade);
    }

    protected void verifyAuthenticationContextSignature(final Pair<? extends SignableSAMLObject, MessageContext> authenticationContext,
                                                        final HttpServletRequest request, final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                        final SamlRegisteredService registeredService) throws Throwable {
        val ctx = authenticationContext.getValue();
        verifyAuthenticationContextSignature(ctx, request, authnRequest, adaptor, registeredService);
    }

    protected void verifyAuthenticationContextSignature(final MessageContext ctx,
                                                        final HttpServletRequest request,
                                                        final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                        final SamlRegisteredService registeredService) throws Throwable {
        if (!SAMLBindingSupport.isMessageSigned(ctx)) {
            LOGGER.trace("The authentication context is not signed");
            if (adaptor.isAuthnRequestsSigned() && !registeredService.isSkipValidatingAuthnRequest()) {
                LOGGER.error("Metadata for [{}] says authentication requests are signed, yet request is not", adaptor.getEntityId());
                throw new SAMLException("Request is not signed but should be");
            }
            LOGGER.trace("Request is not signed or validation is skipped, so there is no need to verify its signature.");
        } else if (adaptor.isAuthnRequestsSigned() && !registeredService.isSkipValidatingAuthnRequest()) {
            LOGGER.trace("The authentication context is signed; Proceeding to validate signatures...");
            configurationContext.getSamlObjectSignatureValidator().verifySamlProfileRequest(authnRequest, adaptor, request, ctx);
        }
    }

    protected Pair<SamlRegisteredService, SamlRegisteredServiceMetadataAdaptor> getRegisteredServiceAndFacade(
        final AuthnRequest authnRequest, final HttpServletRequest httpServletRequest) {
        val issuer = SamlIdPUtils.getIssuerFromSamlObject(authnRequest);
        LOGGER.debug("Located issuer [{}] from authentication context", issuer);

        val registeredService = verifySamlRegisteredService(issuer, httpServletRequest);

        LOGGER.debug("Located SAML metadata for [{}]", registeredService.getServiceId());
        val adaptor = getSamlMetadataFacadeFor(registeredService, authnRequest);

        if (adaptor.isEmpty()) {
            throw UnauthorizedServiceException.denied("Cannot find metadata linked to %s".formatted(issuer));
        }
        val facade = adaptor.get();
        return Pair.of(registeredService, facade);
    }

    protected MessageContext decodeSoapRequest(final HttpServletRequest request) {
        return FunctionUtils.doAndHandle(new CheckedSupplier<MessageContext>() {
            @Override
            public MessageContext get() throws Throwable {
                val decoder = new HTTPSOAP11Decoder();
                decoder.setParserPool(configurationContext.getOpenSamlConfigBean().getParserPool());
                decoder.setHttpServletRequestSupplier(() -> request);

                val binding = new BindingDescriptor();
                binding.setId(getClass().getName());
                binding.setShortName(getClass().getName());
                binding.setSignatureCapable(true);
                binding.setSynchronous(true);

                decoder.setBindingDescriptor(binding);
                decoder.initialize();
                decoder.decode();
                return decoder.getMessageContext();
            }
        }, throwable -> null).get();
    }

    protected void autoConfigureCookiePath(final HttpServletRequest request) {
        val casProperties = configurationContext.getCasProperties();
        val core = casProperties.getAuthn().getSamlIdp().getCore();
        val sessionStorageType = core.getSessionStorageType();
        if (sessionStorageType.isTicketRegistry()
            && core.getSessionReplication().getCookie().isAutoConfigureCookiePath()) {
            val cookieBuilder = configurationContext.getSamlDistributedSessionCookieGenerator();
            CookieUtils.configureCookiePath(request, cookieBuilder);
        }
    }

    protected ModelAndView handleSsoPostProfileRequest(final HttpServletResponse response,
                                                       final HttpServletRequest request,
                                                       final BaseHttpServletRequestXMLMessageDecoder decoder) {
        return FunctionUtils.doAndHandle(() -> {
            val result = getConfigurationContext().getSamlHttpRequestExtractor()
                .extract(request, decoder, AuthnRequest.class)
                .orElseThrow(() -> new IllegalArgumentException("Unable to extract SAML request"));
            val context = Pair.of((AuthnRequest) result.getLeft(), result.getRight());
            return initiateAuthenticationRequest(context, response, request);
        }, WebUtils::produceErrorView).get();
    }

    protected final Pair<? extends RequestAbstractType, MessageContext> retrieveAuthenticationRequest(
        final HttpServletResponse response, final HttpServletRequest request) {
        return lock.tryLock(() -> {
            LOGGER.info("Received SAML2 callback profile request [{}]", request.getRequestURI());
            val webContext = new JEEContext(request, response);
            return SamlIdPSessionManager.of(configurationContext.getOpenSamlConfigBean(), configurationContext.getSessionStore())
                .fetch(webContext, AuthnRequest.class)
                .orElseThrow(() -> {
                    val samlAuthnRequestId = webContext.getRequestParameter(SamlIdPConstants.AUTHN_REQUEST_ID).orElse("N/A");
                    val message = """
                        SAML2 authentication request cannot be determined from the CAS session store for request id %s.
                        This typically means that the original SAML2 authentication request that was submitted to CAS via a SAML2 service provider
                        cannot be retrieved and restored after an authentication attempt. If you are running a multi-node CAS deployment, you may
                        need to opt for a different session storage mechanism than what is configured now: %s
                        """;
                    return new MissingSamlAuthnRequestException(message.stripIndent().stripLeading().trim()
                        .formatted(samlAuthnRequestId, configurationContext.getSessionStore().getClass().getName()));
                });
        });
    }

    protected void storeAuthenticationRequest(final HttpServletRequest request, final HttpServletResponse response,
                                              final Pair<? extends SignableSAMLObject, MessageContext> context) {
        lock.tryLock(__ -> {
            val webContext = new JEEContext(request, response);
            SamlIdPSessionManager.of(configurationContext.getOpenSamlConfigBean(),
                configurationContext.getSessionStore()).store(webContext, context);
        });
    }

    protected String determineProfileBinding(final Pair<? extends RequestAbstractType, MessageContext> authenticationContext,
                                             final HttpServletRequest request) {
        val authnRequest = (AuthnRequest) authenticationContext.getKey();
        val pair = getRegisteredServiceAndFacade(authnRequest, request);
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
