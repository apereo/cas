package org.apereo.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.StringReader;
import java.util.Map;

/**
 * This is {@link ECPProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ECPProfileHandlerController extends AbstractSamlProfileHandlerController {
    /**
     * Instantiates a new ecp saml profile handler controller.
     *
     * @param samlObjectSigner                             the saml object signer
     * @param parserPool                                   the parser pool
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
     */
    public ECPProfileHandlerController(final SamlObjectSigner samlObjectSigner,
                                       final ParserPool parserPool,
                                       final ServicesManager servicesManager,
                                       final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                       final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                       final OpenSamlConfigBean configBean,
                                       final SamlProfileSamlResponseBuilder responseBuilder,
                                       final Map<String, String> authenticationContextClassMappings,
                                       final String serverPrefix,
                                       final String serverName,
                                       final String authenticationContextRequestParameter,
                                       final String loginUrl,
                                       final String logoutUrl,
                                       final boolean forceSignedLogoutRequests,
                                       final boolean singleLogoutCallbacksDisabled) {
        super(samlObjectSigner, parserPool, servicesManager, webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean, responseBuilder, authenticationContextClassMappings, serverPrefix, serverName,
                authenticationContextRequestParameter, loginUrl, logoutUrl, forceSignedLogoutRequests, singleLogoutCallbacksDisabled);
    }

    /**
     * Handle ecp request.
     *
     * @param response the response
     * @param request  the request
     * @param body     the body
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO, consumes = MediaType.TEXT_XML_VALUE)
    public void handleEcpRequest(final HttpServletResponse response,
                                 final HttpServletRequest request,
                                 @RequestBody final String body) throws Exception {

    }


}
