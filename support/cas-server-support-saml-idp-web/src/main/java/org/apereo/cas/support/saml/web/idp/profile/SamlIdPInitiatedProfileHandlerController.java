package org.apereo.cas.support.saml.web.idp.profile;

import module java.base;
import module java.xml;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.web.BrowserStorage;
import com.google.common.primitives.Longs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.binding.SAMLBindingSupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.RequesterID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SamlIdPInitiatedProfileHandlerController}, which handles unsolicited SSO. The authentication request is
 * built here for a service provider whose metadata is known and for an assertion consumer service that the metadata lists;
 * it is neither signed nor checked for a signature.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Tag(name = "SAML2")
public class SamlIdPInitiatedProfileHandlerController extends AbstractSamlIdPProfileHandlerController {

    private static final List<String> PARAMETERS_TO_IGNORE = List.of(
        SamlIdPConstants.TARGET,
        SamlIdPConstants.TIME,
        SamlIdPConstants.SIG_ALG,
        SamlIdPConstants.SIGNATURE,
        SamlIdPConstants.SHIRE,
        SamlIdPConstants.PROVIDER_ID,
        SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE,
        SamlProtocolConstants.PARAMETER_SAML_REQUEST,
        SamlProtocolConstants.PARAMETER_SAML_RESPONSE);

    public SamlIdPInitiatedProfileHandlerController(final SamlProfileHandlerConfigurationContext ctx) {
        super(ctx);
    }

    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO)
    @Operation(summary = "Handle SAML IdP initiated SSO request")
    protected ModelAndView handleIdPInitiatedSsoRequest(final HttpServletResponse response,
                                                        final HttpServletRequest request)
            throws Throwable {
        val providerId = extractProviderId(request);
        val registeredService = verifySamlRegisteredService(providerId, request);
        val adaptor = getSamlMetadataFacadeFor(registeredService, providerId);
        if (adaptor.isEmpty()) {
            throw UnauthorizedServiceException.denied("Cannot find metadata linked to %s".formatted(providerId));
        }

        val facade = adaptor.get();
        val shire = extractShire(request, providerId, facade);

        val target = request.getParameter(SamlIdPConstants.TARGET);
        val time = request.getParameter(SamlIdPConstants.TIME);

        val authnRequest = buildAuthnRequest(providerId, shire, time);
        if (StringUtils.isNotBlank(target)) {
            request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, target);
        }
        val ctx = new MessageContext();
        ctx.setMessage(authnRequest);
        val bindingContext = ctx.ensureSubcontext(SAMLBindingContext.class);
        Objects.requireNonNull(bindingContext).setHasBindingSignature(false);
        SAMLBindingSupport.setRelayState(ctx, target);

        val pair = Pair.<RequestAbstractType, MessageContext>of(authnRequest, ctx);
        val modelAndView = initiateAuthenticationRequest(pair, response, request);
        if (modelAndView != null) {
            if (modelAndView.getView() instanceof final RedirectView view) {
                view.setUrl(appendRequestParameters(request, Objects.requireNonNull(view.getUrl())));
            } else if (modelAndView.getModel().get(BrowserStorage.PARAMETER_BROWSER_STORAGE) instanceof final BrowserStorage storage) {
                storage.setDestinationUrl(appendRequestParameters(request, storage.getDestinationUrl()));
            }
        }
        return modelAndView;
    }

    /**
     * The authentication request of unsolicited SSO is built by this identity provider, not sent by the service provider,
     * so there is no signature to verify, whatever the metadata of the service provider says about signed requests.
     *
     * @param ctx               the message context
     * @param request           the request
     * @param authnRequest      the authentication request
     * @param adaptor           the metadata of the service provider
     * @param registeredService the registered service
     */
    @Override
    protected void verifyAuthenticationContextSignature(final MessageContext ctx,
                                                        final HttpServletRequest request,
                                                        final RequestAbstractType authnRequest,
                                                        final SamlRegisteredServiceMetadataAdaptor adaptor,
                                                        final SamlRegisteredService registeredService) {
        LOGGER.debug("Unsolicited authentication request for [{}] is built by the identity provider and carries no signature", adaptor.getEntityId());
    }

    private static String appendRequestParameters(final HttpServletRequest request, final String url) throws URISyntaxException {
        val urlBuilder = new URIBuilder(url);
        val parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            val parameterName = parameterNames.nextElement();
            if (canAcceptParameter(parameterName)) {
                urlBuilder.addParameter(parameterName, request.getParameter(parameterName));
            }
        }
        return urlBuilder.build().toString();
    }

    private static boolean canAcceptParameter(final String parameterName) {
        return !PARAMETERS_TO_IGNORE.contains(parameterName);
    }

    protected AuthnRequest buildAuthnRequest(final String providerId, final String shire, final String time) {
        val authnRequest = buildSamlObject(AuthnRequest.DEFAULT_ELEMENT_NAME, AuthnRequest.class);
        authnRequest.setAssertionConsumerServiceURL(shire);

        val issuer = buildSamlObject(Issuer.DEFAULT_ELEMENT_NAME, Issuer.class);
        issuer.setValue(providerId);
        authnRequest.setIssuer(issuer);
        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);

        val nameIDPolicy = buildSamlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME, NameIDPolicy.class);
        nameIDPolicy.setAllowCreate(Boolean.TRUE);
        authnRequest.setNameIDPolicy(nameIDPolicy);

        val issueInstant = time != null ? Longs.tryParse(time) : null;
        if (issueInstant != null) {
            authnRequest.setIssueInstant(Instant.ofEpochMilli(issueInstant));
        } else {
            authnRequest.setIssueInstant(ZonedDateTime.now(ZoneOffset.UTC).toInstant());
        }
        authnRequest.setID(Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        authnRequest.setForceAuthn(Boolean.FALSE);

        val extensions = buildSamlObject(Extensions.DEFAULT_ELEMENT_NAME, Extensions.class);
        val requesterId = buildSamlObject(RequesterID.DEFAULT_ELEMENT_NAME, RequesterID.class);
        requesterId.setURI(getConfigurationContext().getCasProperties().getAuthn().getSamlIdp().getCore().getEntityId());
        extensions.getUnknownXMLObjects().add(requesterId);
        authnRequest.setExtensions(extensions);
        return authnRequest;
    }

    protected <T> T buildSamlObject(final QName qname, final Class<T> clazz) {
        val builderFactory = getConfigurationContext().getOpenSamlConfigBean().getBuilderFactory();
        val builder = (SAMLObjectBuilder) builderFactory.getBuilder(qname);
        return clazz.cast(Objects.requireNonNull(builder).buildObject());
    }

    protected String extractShire(final HttpServletRequest request, final String providerId,
                                  final SamlRegisteredServiceMetadataAdaptor facade)
            throws MessageDecodingException {
        var shire = request.getParameter(SamlIdPConstants.SHIRE);
        if (StringUtils.isNotBlank(shire)) {
            val locations = facade.getAssertionConsumerServiceLocations(SAMLConstants.SAML2_POST_BINDING_URI);
            if (!locations.contains(shire)) {
                LOGGER.warn("Assertion consumer service URL [{}] is not registered in metadata for [{}]", shire, providerId);
                throw new MessageDecodingException("Assertion consumer service URL is not registered in service provider metadata");
            }
        } else {
            LOGGER.info("Resolving service provider assertion consumer service URL for [{}] and binding [{}]",
                providerId, SAMLConstants.SAML2_POST_BINDING_URI);
            val acs = facade.getAssertionConsumerService(SAMLConstants.SAML2_POST_BINDING_URI);
            shire = Optional.ofNullable(acs)
                .map(service -> StringUtils.isBlank(service.getResponseLocation())
                    ? service.getLocation()
                    : service.getResponseLocation()).orElse(null);
        }
        if (StringUtils.isBlank(shire)) {
            LOGGER.warn("Unable to resolve service provider assertion consumer service URL for AuthnRequest construction for entityID: [{}]", providerId);
            throw new MessageDecodingException("Unable to resolve SP ACS URL for AuthnRequest construction");
        }
        return shire;
    }

    protected String extractProviderId(final HttpServletRequest request) throws MessageDecodingException {
        val providerId = request.getParameter(SamlIdPConstants.PROVIDER_ID);
        if (StringUtils.isBlank(providerId)) {
            LOGGER.warn("No providerId parameter given in unsolicited SSO authentication request.");
            throw new MessageDecodingException("Missing providerId");
        }
        return providerId;
    }
}
