package org.apereo.cas.support.saml.web.idp.profile.slo;


import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link SLOSamlRedirectProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO Redirects.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SLOSamlRedirectProfileHandlerController extends AbstractSamlSLOProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SLOSamlRedirectProfileHandlerController.class);

    /**
     * Instantiates a new slo saml profile handler controller.
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
    public SLOSamlRedirectProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                                   final ParserPool parserPool,
                                                   final AuthenticationSystemSupport authenticationSystemSupport,
                                                   final ServicesManager servicesManager,
                                                   final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                                   final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                                   final OpenSamlConfigBean configBean,
                                                   final SamlProfileObjectBuilder<Response> responseBuilder,
                                                   final CasConfigurationProperties casProperties,
                                                   final SamlObjectSignatureValidator samlObjectSignatureValidator) {
        super(samlObjectSigner,
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean,
                responseBuilder,
                casProperties,
                samlObjectSignatureValidator);
    }

    /**
     * Handle SLO Redirect profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_REDIRECT)
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleSloProfileRequest(response, request, new HTTPRedirectDeflateDecoder());
    }
}
