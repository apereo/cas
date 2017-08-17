package org.apereo.cas.support.saml.web.idp.profile;

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
import org.opensaml.saml.common.SAMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link ArtifactResolutionProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ArtifactResolutionProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResolutionProfileHandlerController.class);

    public ArtifactResolutionProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner, final ParserPool parserPool, 
                                                      final AuthenticationSystemSupport authenticationSystemSupport, 
                                                      final ServicesManager servicesManager, 
                                                      final ServiceFactory<WebApplicationService> webApplicationServiceFactory, 
                                                      final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver, 
                                                      final OpenSamlConfigBean configBean, 
                                                      final SamlProfileObjectBuilder<? extends SAMLObject> responseBuilder, 
                                                      final CasConfigurationProperties casProperties, 
                                                      final SamlObjectSignatureValidator samlObjectSignatureValidator) {
        super(samlObjectSigner, parserPool, authenticationSystemSupport, servicesManager, 
                webApplicationServiceFactory, samlRegisteredServiceCachingMetadataResolver, configBean, 
                responseBuilder, casProperties, samlObjectSignatureValidator);
    }

    /**
     * Handle get request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @GetMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SOAP_ARTIFACT_RESOLUTION)
    protected void handleGetRequest(final HttpServletResponse response,
                                    final HttpServletRequest request) throws Exception {
    }

    /**
     * Handle post request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_SOAP_ARTIFACT_RESOLUTION)
    protected void handlePostRequest(final HttpServletResponse response,
                                     final HttpServletRequest request) throws Exception {
    }
}
