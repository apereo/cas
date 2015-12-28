package org.jasig.cas.support.saml.web.idp.profile.builders;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.cryptacular.util.CertUtil;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlMetadataAdaptor;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.binding.security.impl.EndpointURLSchemeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlObjectEncoder}.
 *
 * @author Misagh Moayyed
 * @since 4.3
 */
@Component("samlObjectEncoder")
public class SamlObjectEncoder {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Override signature canonicalization algorithm.
     */
    @Value("${cas.samlidp.response.override.sig.can.alg:}")
    protected String overrideSignatureCanonicalizationAlgorithm;

    /**
     * The Override signature reference digest methods.
     */
    @Autowired(required = false)
    @Qualifier("overrideSignatureReferenceDigestMethods")
    protected List overrideSignatureReferenceDigestMethods;

    /**
     * The Override signature algorithms.
     */
    @Autowired(required = false)
    @Qualifier("overrideSignatureAlgorithms")
    protected List overrideSignatureAlgorithms;

    /**
     * The Override black listed signature signing algorithms.
     */
    @Autowired(required = false)
    @Qualifier("overrideBlackListedSignatureSigningAlgorithms")
    protected List overrideBlackListedSignatureSigningAlgorithms;

    /**
     * The Override white listed signature signing algorithms.
     */
    @Autowired(required = false)
    @Qualifier("overrideWhiteListedSignatureSigningAlgorithms")
    protected List overrideWhiteListedAlgorithms;

    /**
     * The Signing cert file.
     */
    @Value("${cas.samlidp.metadata.location:}/idp-signing.crt")
    protected File signingCertFile;

    /**
     * The Signing key file.
     */
    @Value("${cas.samlidp.metadata.location:}/idp-signing.key")
    protected File signingKeyFile;

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param <T>        the type parameter
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @param response   the response
     * @param request    the request
     * @return the t
     * @throws SamlException the saml exception
     */
    protected final <T extends SAMLObject> T encode(final T samlObject,
                                                    final SamlRegisteredService service,
                                                    final SamlMetadataAdaptor adaptor,
                                                    final HttpServletResponse response,
                                                    final HttpServletRequest request) throws SamlException {
        try {
            logger.debug("Attempting to encode {} for {}", samlObject.getClass().getName(), adaptor.getEntityId());
            final MessageContext<T> outboundContext = new MessageContext<>();
            prepareOutboundContext(samlObject, adaptor, outboundContext);
            prepareSecurityParametersContext(adaptor, outboundContext);
            prepareEndpointURLSchemeSecurityHandler(outboundContext);
            prepareSAMLOutboundDestinationHandler(outboundContext);
            prepareSAMLOutboundProtocolMessageSigningHandler(outboundContext);
            return samlObject;
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    /**
     * Prepare saml outbound protocol message signing handler.
     *
     * @param <T>             the type parameter
     * @param outboundContext the outbound context
     * @throws Exception the exception
     */
    protected <T extends SAMLObject> void prepareSAMLOutboundProtocolMessageSigningHandler(final MessageContext<T> outboundContext)
            throws Exception {
        final SAMLOutboundProtocolMessageSigningHandler handler = new SAMLOutboundProtocolMessageSigningHandler();
        handler.setSignErrorResponses(false);
        handler.invoke(outboundContext);
    }

    /**
     * Prepare saml outbound destination handler.
     *
     * @param <T>             the type parameter
     * @param outboundContext the outbound context
     * @throws Exception the exception
     */
    protected <T extends SAMLObject> void prepareSAMLOutboundDestinationHandler(final MessageContext<T> outboundContext)
            throws Exception {
        final SAMLOutboundDestinationHandler handlerDest = new SAMLOutboundDestinationHandler();
        handlerDest.initialize();
        handlerDest.invoke(outboundContext);
    }

    /**
     * Prepare endpoint url scheme security handler.
     *
     * @param <T>             the type parameter
     * @param outboundContext the outbound context
     * @throws Exception the exception
     */
    protected <T extends SAMLObject> void prepareEndpointURLSchemeSecurityHandler(final MessageContext<T> outboundContext)
            throws Exception {
        final EndpointURLSchemeSecurityHandler handlerEnd = new EndpointURLSchemeSecurityHandler();
        handlerEnd.initialize();
        handlerEnd.invoke(outboundContext);
    }

    /**
     * Prepare security parameters context.
     *
     * @param <T>             the type parameter
     * @param adaptor         the adaptor
     * @param outboundContext the outbound context
     * @throws SAMLException the saml exception
     */
    protected <T extends SAMLObject> void prepareSecurityParametersContext(final SamlMetadataAdaptor adaptor,
                                                                           final MessageContext<T> outboundContext) throws SAMLException {
        final SecurityParametersContext secParametersContext = outboundContext.getSubcontext(SecurityParametersContext.class, true);
        if (secParametersContext == null) {
            throw new RuntimeException("No signature signing parameters could be determined");
        }
        final SignatureSigningParameters signingParameters = buildSignatureSigningParameters(adaptor.getSsoDescriptor());
        secParametersContext.setSignatureSigningParameters(signingParameters);
    }

    /**
     * Prepare outbound context.
     *
     * @param <T>             the type parameter
     * @param samlObject      the saml object
     * @param adaptor         the adaptor
     * @param outboundContext the outbound context
     */
    protected <T extends SAMLObject> void prepareOutboundContext(final T samlObject, final SamlMetadataAdaptor adaptor,
                                                                 final MessageContext<T> outboundContext) {

        logger.debug("Outbound saml object to use is {}", samlObject.getClass().getName());
        outboundContext.setMessage(samlObject);
        final List<AssertionConsumerService> assertionConsumerServices = adaptor.getAssertionConsumerServices();
        final SAMLPeerEntityContext peerEntityContext = outboundContext.getSubcontext(SAMLPeerEntityContext.class, true);
        if (peerEntityContext != null) {
            final SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
            if (endpointContext != null) {
                final Endpoint endpoint = assertionConsumerServices.get(0);
                logger.debug("Configured peer entity endpoint to be {} with binding {}", endpoint.getLocation(), endpoint.getBinding());
                endpointContext.setEndpoint(assertionConsumerServices.get(0));
            }
        }
    }

    /**
     * Build signature signing parameters signature signing parameters.
     *
     * @param descriptor the descriptor
     * @return the signature signing parameters
     * @throws SAMLException the saml exception
     */
    protected SignatureSigningParameters buildSignatureSigningParameters(final RoleDescriptor descriptor) throws SAMLException {
        try {
            final CriteriaSet criteria = new CriteriaSet();
            criteria.add(new SignatureSigningConfigurationCriterion(getSignatureSigningConfiguration()));
            criteria.add(new RoleDescriptorCriterion(descriptor));
            final SAMLMetadataSignatureSigningParametersResolver resolver = new SAMLMetadataSignatureSigningParametersResolver();
            logger.debug("Resolving signature signing parameters for {}", descriptor.getElementQName().getLocalPart());

            final SignatureSigningParameters params = resolver.resolveSingle(criteria);
            if (params == null) {
                throw new SAMLException("No signature signing parameter is available");
            }

            logger.debug("Created signature signing parameters."
                            + "\nSignature algorithm: {}"
                            + "\nSignature canonicalization algorithm: {}"
                            + "\nSignature reference digest methods: {}",
                    params.getSignatureAlgorithm(), params.getSignatureCanonicalizationAlgorithm(),
                    params.getSignatureReferenceDigestMethod());

            return params;
        } catch (final Exception e) {
            throw new SAMLException(e.getMessage(), e);
        }
    }

    /**
     * Gets signature signing configuration.
     *
     * @return the signature signing configuration
     * @throws Exception the exception
     */
    protected SignatureSigningConfiguration getSignatureSigningConfiguration() throws Exception {
        final BasicSignatureSigningConfiguration config =
                DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();


        if (this.overrideBlackListedSignatureSigningAlgorithms != null && !this.overrideSignatureCanonicalizationAlgorithm.isEmpty()) {
            config.setBlacklistedAlgorithms(this.overrideBlackListedSignatureSigningAlgorithms);
        }

        if (this.overrideSignatureAlgorithms != null && !this.overrideSignatureAlgorithms.isEmpty()) {
            config.setSignatureAlgorithms(this.overrideSignatureAlgorithms);
        }

        if (this.overrideSignatureReferenceDigestMethods != null && !this.overrideSignatureReferenceDigestMethods.isEmpty()) {
            config.setSignatureReferenceDigestMethods(this.overrideSignatureReferenceDigestMethods);
        }

        if (this.overrideWhiteListedAlgorithms != null && !this.overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(this.overrideWhiteListedAlgorithms);
        }


        if (StringUtils.isNotBlank(overrideSignatureCanonicalizationAlgorithm)) {
            config.setSignatureCanonicalizationAlgorithm(this.overrideSignatureCanonicalizationAlgorithm);
        }
        logger.debug("Signature signing blacklisted algorithms: {}", config.getBlacklistedAlgorithms());
        logger.debug("Signature signing signature algorithms: {}", config.getSignatureAlgorithms());
        logger.debug("Signature signing signature canonicalization algorithm: {}", config.getSignatureCanonicalizationAlgorithm());
        logger.debug("Signature signing whitelisted algorithms: {}", config.getWhitelistedAlgorithms());
        logger.debug("Signature signing reference digest methods: {}", config.getSignatureReferenceDigestMethods());

        final PrivateKeyFactoryBean privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(new FileSystemResource(this.signingKeyFile));
        privateKeyFactoryBean.setAlgorithm("RSA");
        privateKeyFactoryBean.setSingleton(false);
        logger.debug("Locating signature signing key file from {}", this.signingKeyFile);
        final PrivateKey privateKey = privateKeyFactoryBean.getObject();

        logger.debug("Locating signature signing certificate file from {}", this.signingCertFile);
        final X509Certificate certificate = readCertificate(new FileSystemResource(this.signingCertFile));
        final List<Credential> creds = new ArrayList<>();
        creds.add(new BasicX509Credential(certificate, privateKey));
        config.setSigningCredentials(creds);
        logger.debug("Signature signing credentials configured");

        return config;
    }

    /**
     * Read certificate x 509 certificate.
     *
     * @param resource the resource
     * @return the x 509 certificate
     */
    private static X509Certificate readCertificate(final Resource resource) {
        try (final InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new RuntimeException("Error reading certificate " + resource, e);
        }
    }


}
