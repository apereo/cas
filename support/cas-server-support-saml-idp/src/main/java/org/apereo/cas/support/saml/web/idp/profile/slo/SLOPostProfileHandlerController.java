package org.apereo.cas.support.saml.web.idp.profile.slo;


import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * This is {@link SLOPostProfileHandlerController}, responsible for
 * handling requests for SAML2 SLO.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SLOPostProfileHandlerController extends AbstractSamlSLOProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SLOPostProfileHandlerController.class);

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
     * @param authenticationContextClassMappings           the authentication context class mappings
     * @param serverPrefix                                 the server prefix
     * @param serverName                                   the server name
     * @param authenticationContextRequestParameter        the authentication context request parameter
     * @param loginUrl                                     the login url
     * @param logoutUrl                                    the logout url
     * @param forceSignedLogoutRequests                    the force signed logout requests
     * @param singleLogoutCallbacksDisabled                the single logout callbacks disabled
     * @param samlObjectSignatureValidator                 the saml object signature validator
     */
    public SLOPostProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                           final ParserPool parserPool,
                                           final AuthenticationSystemSupport authenticationSystemSupport,
                                           final ServicesManager servicesManager,
                                           final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                           final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                           final OpenSamlConfigBean configBean,
                                           final SamlProfileObjectBuilder<Response> responseBuilder,
                                           final Set<String> authenticationContextClassMappings,
                                           final String serverPrefix,
                                           final String serverName,
                                           final String authenticationContextRequestParameter,
                                           final String loginUrl,
                                           final String logoutUrl,
                                           final boolean forceSignedLogoutRequests,
                                           final boolean singleLogoutCallbacksDisabled,
                                           final SamlObjectSignatureValidator samlObjectSignatureValidator) {
        super(samlObjectSigner,
                parserPool,
                authenticationSystemSupport,
                servicesManager,
                webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean,
                responseBuilder,
                authenticationContextClassMappings,
                serverPrefix,
                serverName,
                authenticationContextRequestParameter,
                loginUrl,
                logoutUrl,
                forceSignedLogoutRequests,
                singleLogoutCallbacksDisabled,
                samlObjectSignatureValidator);
    }

    /**
     * Handle SLO POST profile request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SLO_PROFILE_POST)
    protected void handleSaml2ProfileSLOPostRequest(final HttpServletResponse response,
                                                    final HttpServletRequest request) throws Exception {
        handleSloProfileRequest(response, request, new HTTPPostDecoder());
    }
}
