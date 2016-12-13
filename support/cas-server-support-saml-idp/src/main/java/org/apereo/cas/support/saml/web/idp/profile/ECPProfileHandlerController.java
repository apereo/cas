package org.apereo.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileSamlResponseBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSigner;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.util.XMLObjectSupport;
import org.opensaml.saml.common.binding.BindingDescriptor;
import org.opensaml.saml.common.binding.impl.SAMLSOAPDecoderBodyHandler;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        final XMLObject objectBody = XMLObjectSupport.unmarshallFromReader(parserPool, new StringReader(body));
        SamlUtils.logSamlObject(configBean, objectBody);

        final Credential credential = extractBasicAuthenticationCredential(request, response);
        final SOAP11Context soapContext = decodeSoapRequest(request);
        if (credential == null) {
            logger.error("Credentials could not be extracted from the SAML ECP request");
            return;
        }
        if (soapContext == null) {
            logger.error("SAML ECP request could not be determined from the authentication request");
            return;
        }

    }

    /**
     * Decode soap 11 context.
     *
     * @param request the request
     * @return the soap 11 context
     * @throws Exception the exception
     */
    protected SOAP11Context decodeSoapRequest(final HttpServletRequest request) {
        try {
            final HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();
            decoder.setParserPool(parserPool);
            decoder.setHttpServletRequest(request);
            decoder.setBindingDescriptor(new BindingDescriptor());
            decoder.setBodyHandler(new SAMLSOAPDecoderBodyHandler());
            decoder.initialize();
            decoder.decode();
            return decoder.getMessageContext().getSubcontext(SOAP11Context.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private Credential extractBasicAuthenticationCredential(final HttpServletRequest request,
                                                            final HttpServletResponse response) {
        final BasicAuthExtractor extractor = new BasicAuthExtractor(this.getClass().getSimpleName());
        final WebContext webContext = new J2EContext(request, response);
        try {
            final UsernamePasswordCredentials credentials = extractor.extract(webContext);
            if (credentials != null) {
                logger.debug("Received basic authentication ECP request from credentials {} ", credentials);
                return new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
            }
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }
}
