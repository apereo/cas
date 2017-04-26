package org.apereo.cas.support.saml.web.idp.profile;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.BaseSamlObjectSigner;
import org.apereo.cas.support.saml.web.idp.profile.builders.enc.SamlObjectSignatureValidator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.support.WebUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.BindingDescriptor;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Envelope;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ECPProfileHandlerController}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ECPProfileHandlerController extends AbstractSamlProfileHandlerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ECPProfileHandlerController.class);

    private final SamlProfileObjectBuilder<? extends SAMLObject> samlEcpFaultResponseBuilder;

    /**
     * Instantiates a new ecp saml profile handler controller.
     *
     * @param samlObjectSigner                             the saml object signer
     * @param parserPool                                   the parser pool
     * @param authenticationSystemSupport                  the authentication system support
     * @param servicesManager                              the services manager
     * @param webApplicationServiceFactory                 the web application service factory
     * @param samlRegisteredServiceCachingMetadataResolver the saml registered service caching metadata resolver
     * @param configBean                                   the config bean
     * @param responseBuilder                              the response builder
     * @param samlEcpFaultResponseBuilder                  the saml ecp fault response builder
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
    public ECPProfileHandlerController(final BaseSamlObjectSigner samlObjectSigner,
                                       final ParserPool parserPool,
                                       final AuthenticationSystemSupport authenticationSystemSupport,
                                       final ServicesManager servicesManager,
                                       final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
                                       final SamlRegisteredServiceCachingMetadataResolver samlRegisteredServiceCachingMetadataResolver,
                                       final OpenSamlConfigBean configBean,
                                       final SamlProfileObjectBuilder<org.opensaml.saml.saml2.ecp.Response> responseBuilder,
                                       final SamlProfileObjectBuilder<? extends SAMLObject> samlEcpFaultResponseBuilder,
                                       final Set<String> authenticationContextClassMappings,
                                       final String serverPrefix,
                                       final String serverName,
                                       final String authenticationContextRequestParameter,
                                       final String loginUrl,
                                       final String logoutUrl,
                                       final boolean forceSignedLogoutRequests,
                                       final boolean singleLogoutCallbacksDisabled,
                                       final SamlObjectSignatureValidator samlObjectSignatureValidator) {
        super(samlObjectSigner, parserPool, authenticationSystemSupport,
                servicesManager, webApplicationServiceFactory,
                samlRegisteredServiceCachingMetadataResolver,
                configBean, responseBuilder,
                authenticationContextClassMappings,
                serverPrefix, serverName,
                authenticationContextRequestParameter, loginUrl, logoutUrl,
                forceSignedLogoutRequests, singleLogoutCallbacksDisabled,
                samlObjectSignatureValidator);
        this.samlEcpFaultResponseBuilder = samlEcpFaultResponseBuilder;
    }

    /**
     * Handle ecp request.
     *
     * @param response the response
     * @param request  the request
     * @throws Exception the exception
     */
    @PostMapping(path = SamlIdPConstants.ENDPOINT_SAML2_IDP_ECP_PROFILE_SSO,
            consumes = {MediaType.TEXT_XML_VALUE, SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE},
            produces = {MediaType.TEXT_XML_VALUE, SamlIdPConstants.ECP_SOAP_PAOS_CONTENT_TYPE})
    public void handleEcpRequest(final HttpServletResponse response,
                                 final HttpServletRequest request) throws Exception {
        final MessageContext soapContext = decodeSoapRequest(request);
        final Credential credential = extractBasicAuthenticationCredential(request, response);

        if (credential == null) {
            LOGGER.error("Credentials could not be extracted from the SAML ECP request");
            return;
        }
        if (soapContext == null) {
            LOGGER.error("SAML ECP request could not be determined from the authentication request");
            return;
        }
        handleEcpRequest(response, request, soapContext, credential, SAMLConstants.SAML2_PAOS_BINDING_URI);
    }

    /**
     * Handle ecp request.
     *
     * @param response    the response
     * @param request     the request
     * @param soapContext the soap context
     * @param credential  the credential
     * @param binding     the binding
     */
    protected void handleEcpRequest(final HttpServletResponse response, final HttpServletRequest request,
                                    final MessageContext soapContext, final Credential credential,
                                    final String binding) {
        LOGGER.debug("Handling ECP request for SOAP context [{}]", soapContext);

        final Envelope envelope = soapContext.getSubcontext(SOAP11Context.class).getEnvelope();
        SamlUtils.logSamlObject(configBean, envelope);

        final AuthnRequest authnRequest = (AuthnRequest) soapContext.getMessage();
        final Pair<AuthnRequest, MessageContext> authenticationContext = Pair.of(authnRequest, soapContext);
        try {
            LOGGER.debug("Verifying ECP authentication request [{}]", authnRequest);
            final Pair<SamlRegisteredService, SamlRegisteredServiceServiceProviderMetadataFacade> serviceRequest =
                    verifySamlAuthenticationRequest(authenticationContext, request);

            LOGGER.debug("Attempting to authenticate ECP request for credential id [{}]", credential.getId());
            final Authentication authentication = authenticateEcpRequest(credential, authenticationContext);
            LOGGER.debug("Authenticated [{}] successfully with authenticated principal [{}]",
                    credential.getId(), authentication.getPrincipal());

            LOGGER.debug("Building ECP SAML response for [{}]", credential.getId());
            final String issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest);
            final Service service = webApplicationServiceFactory.createService(issuer);
            final Assertion casAssertion = buildEcpCasAssertion(authentication, service, serviceRequest.getKey());

            LOGGER.debug("CAS assertion to use for building ECP SAML response is [{}]", casAssertion);
            buildSamlResponse(response, request, authenticationContext, casAssertion, binding);
        } catch (final AuthenticationException e) {
            LOGGER.error(e.getMessage(), e);
            final String error = e.getHandlerErrors().values()
                    .stream()
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(","));
            buildEcpFaultResponse(response, request, Pair.of(authnRequest, error));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            buildEcpFaultResponse(response, request, Pair.of(authnRequest, e.getMessage()));
        }
    }

    /**
     * Build ecp fault response.
     *
     * @param response              the response
     * @param request               the request
     * @param authenticationContext the authentication context
     */
    protected void buildEcpFaultResponse(final HttpServletResponse response,
                                         final HttpServletRequest request,
                                         final Pair<AuthnRequest, String> authenticationContext) {
        request.setAttribute(SamlIdPConstants.REQUEST_ATTRIBUTE_ERROR, authenticationContext.getValue());
        samlEcpFaultResponseBuilder.build(authenticationContext.getKey(), request, response,
                null, null, null, SAMLConstants.SAML2_PAOS_BINDING_URI);

    }

    /**
     * Authenticate ecp request.
     *
     * @param credential   the credential
     * @param authnRequest the authn request
     * @return the authentication
     */
    protected Authentication authenticateEcpRequest(final Credential credential,
                                                    final Pair<AuthnRequest, MessageContext> authnRequest) {
        final String issuer = SamlIdPUtils.getIssuerFromSamlRequest(authnRequest.getKey());
        LOGGER.debug("Located issuer [{}] from request prior to authenticating [{}]", issuer, credential.getId());

        final Service service = webApplicationServiceFactory.createService(issuer);
        LOGGER.debug("Executing authentication request for service [{}] on behalf of credential id [{}]", service, credential.getId());
        final AuthenticationResult authenticationResult = authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
        return authenticationResult.getAuthentication();
    }

    /**
     * Build ecp cas assertion assertion.
     *
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the assertion
     */
    protected Assertion buildEcpCasAssertion(final Authentication authentication,
                                             final Service service,
                                             final RegisteredService registeredService) {
        final Map attributes = registeredService.getAttributeReleasePolicy()
                .getAttributes(authentication.getPrincipal(), service, registeredService);
        final AttributePrincipal principal = new AttributePrincipalImpl(
                authentication.getPrincipal().getId(), attributes);
        return new AssertionImpl(principal, DateTimeUtils.dateOf(authentication.getAuthenticationDate()),
                null, DateTimeUtils.dateOf(authentication.getAuthenticationDate()),
                authentication.getAttributes());
    }

    /**
     * Decode soap 11 context.
     *
     * @param request the request
     * @return the soap 11 context
     */
    protected MessageContext decodeSoapRequest(final HttpServletRequest request) {
        try {
            final HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();
            decoder.setParserPool(parserPool);
            decoder.setHttpServletRequest(request);

            final BindingDescriptor binding = new BindingDescriptor();
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

    private Credential extractBasicAuthenticationCredential(final HttpServletRequest request,
                                                            final HttpServletResponse response) {
        try {
            final BasicAuthExtractor extractor = new BasicAuthExtractor(this.getClass().getSimpleName());
            final WebContext webContext = WebUtils.getPac4jJ2EContext(request, response);
            final UsernamePasswordCredentials credentials = extractor.extract(webContext);
            if (credentials != null) {
                LOGGER.debug("Received basic authentication ECP request from credentials [{}]", credentials);
                return new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }
}
