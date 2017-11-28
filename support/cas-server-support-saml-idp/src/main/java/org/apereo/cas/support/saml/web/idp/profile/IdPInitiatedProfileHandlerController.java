package org.apereo.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.jasig.cas.client.util.CommonUtils;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link IdPInitiatedProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class IdPInitiatedProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdPInitiatedProfileHandlerController.class);

    /**
     * Instantiates a new idp-init saml profile handler controller.
     *
     * @param samlObjectSigner                             the saml object signer
     * @param parserPool                                   the parser pool
     * @param authenticationSystemSupport                  the authentication system support
     * @param servicesManager                              the services manager
     * @param webApplicationServiceFactory                 the web application service factory
     * @param samlRegisteredServiceCachingMetadataResolver the saml registered service caching metadata resolver
     * @param configBean                                   the config bean
     * @param responseBuilder                              the response builder
     * @param casProperties                                the cas properties
     * @param samlObjectSignatureValidator                 the saml object signature validator
     */
    public IdPInitiatedProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                                final ParserPool parserPool,
                                                final AuthenticationSystemSupport authenticationSystemSupport,
                                                final ServicesManager servicesManager,
                                                final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                final OpenSamlConfigBean configBean,
                                                final SamlProfileObjectBuilder<Response> responseBuilder,
                                                final CasConfigurationProperties casProperties,
                                                final SamlObjectSignatureValidator samlObjectSignatureValidator) {
        super(samlObjectSigner, parserPool, authenticationSystemSupport,
                servicesManager, webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean, responseBuilder, casProperties,
                samlObjectSignatureValidator);
    }

    /**
     * Handle idp initiated sso requests.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_INIT_PROFILE_SSO)
    protected void handleIdPInitiatedSsoRequest(final HttpServletResponse response,
                                                final HttpServletRequest request) throws Exception {

        // The name (i.e., the entity ID) of the service provider.
        final String providerId = CommonUtils.safeGetParameter(request, SamlIdPConstants.PROVIDER_ID);
        if (StringUtils.isBlank(providerId)) {
            LOGGER.warn("No providerId parameter given in unsolicited SSO authentication request.");
            throw new MessageDecodingException("No providerId parameter given in unsolicited SSO authentication request.");
        }

        final SamlRegisteredService registeredService = verifySamlRegisteredService(providerId);
        final Optional<SamlRegisteredServiceServiceProviderMetadataFacade> adaptor = getSamlMetadataFacadeFor(registeredService, providerId);
        if (!adaptor.isPresent()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Cannot find metadata linked to " + providerId);
        }

        // The URL of the response location at the SP (called the "Assertion Consumer Service")
        // but can be omitted in favor of the IdP picking the default endpoint location from metadata.
        String shire = CommonUtils.safeGetParameter(request, SamlIdPConstants.SHIRE);
        final SamlRegisteredServiceServiceProviderMetadataFacade facade = adaptor.get();
        if (StringUtils.isBlank(shire)) {
            final AssertionConsumerService acs = facade.getAssertionConsumerService(SAMLConstants.SAML2_POST_BINDING_URI);
            if (acs == null) {
                throw new MessageDecodingException("Unable to resolve SP ACS URL");
            }
            shire = acs.getLocation();
        }
        if (StringUtils.isBlank(shire)) {
            LOGGER.warn("Unable to resolve SP ACS URL for AuthnRequest construction for entityID: [{}]", providerId);
            throw new MessageDecodingException("Unable to resolve SP ACS URL for AuthnRequest construction");
        }

        // The target resource at the SP, or a state token generated by an SP to represent the resource.
        final String target = CommonUtils.safeGetParameter(request, SamlIdPConstants.TARGET);

        // A timestamp to help with stale request detection.
        final String time = CommonUtils.safeGetParameter(request, SamlIdPConstants.TIME);

        final SAMLObjectBuilder builder = (SAMLObjectBuilder) configBean.getBuilderFactory().getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
        final AuthnRequest authnRequest = (AuthnRequest) builder.buildObject();
        authnRequest.setAssertionConsumerServiceURL(shire);

        final SAMLObjectBuilder isBuilder = (SAMLObjectBuilder) configBean.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
        final Issuer issuer = (Issuer) isBuilder.buildObject();
        issuer.setValue(providerId);
        authnRequest.setIssuer(issuer);

        authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        final SAMLObjectBuilder pBuilder = (SAMLObjectBuilder) configBean.getBuilderFactory().getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
        final NameIDPolicy nameIDPolicy = (NameIDPolicy) pBuilder.buildObject();
        nameIDPolicy.setAllowCreate(Boolean.TRUE);
        authnRequest.setNameIDPolicy(nameIDPolicy);

        if (NumberUtils.isCreatable(time)) {
            authnRequest.setIssueInstant(new DateTime(TimeUnit.SECONDS.convert(Long.parseLong(time), TimeUnit.MILLISECONDS),
                    ISOChronology.getInstanceUTC()));
        } else {
            authnRequest.setIssueInstant(new DateTime(DateTime.now(), ISOChronology.getInstanceUTC()));
        }
        authnRequest.setForceAuthn(Boolean.FALSE);
        if (StringUtils.isNotBlank(target)) {
            request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, target);
        }

        final MessageContext ctx = new MessageContext();
        ctx.setAutoCreateSubcontexts(true);

        if (facade.isAuthnRequestsSigned()) {
            samlObjectSigner.encode(authnRequest, registeredService,
                    facade, response, request, SAMLConstants.SAML2_POST_BINDING_URI);
        }
        ctx.setMessage(authnRequest);
        ctx.getSubcontext(SAMLBindingContext.class, true).setHasBindingSignature(false);

        final Pair<SignableSAMLObject, MessageContext> pair = Pair.of(authnRequest, ctx);
        initiateAuthenticationRequest(pair, response, request);
    }
}
