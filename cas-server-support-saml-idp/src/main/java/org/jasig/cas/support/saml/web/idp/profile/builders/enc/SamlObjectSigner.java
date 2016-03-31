package org.jasig.cas.support.saml.web.idp.profile.builders.enc;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.SamlIdPUtils;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.util.PrivateKeyFactoryBean;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.binding.security.impl.EndpointURLSchemeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.criterion.SignatureValidationConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.impl.BasicSignatureValidationConfiguration;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlObjectSigner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("samlObjectSigner")
public class SamlObjectSigner {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

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
     * The Sign error response.
     */
    @Value("${cas.samlidp.response.error.sign:false}")
    protected boolean signErrorResponse;


    @Value("${cas.samlidp.key.private.alg:RSA}")
    private String privateKeyAlgName;

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
    public <T extends SAMLObject> T encode(final T samlObject,
                                                 final SamlRegisteredService service,
                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                 final HttpServletResponse response,
                                                 final HttpServletRequest request) throws SamlException {
        try {
            logger.debug("Attempting to encode [{}] for [{}]", samlObject.getClass().getName(), adaptor.getEntityId());
            final MessageContext<T> outboundContext = new MessageContext<>();
            prepareOutboundContext(samlObject, adaptor, outboundContext);
            prepareSecurityParametersContext(adaptor, outboundContext);
            prepareEndpointURLSchemeSecurityHandler(outboundContext);
            prepareSamlOutboundDestinationHandler(outboundContext);
            prepareSamlOutboundProtocolMessageSigningHandler(outboundContext);
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
    protected <T extends SAMLObject> void prepareSamlOutboundProtocolMessageSigningHandler(final MessageContext<T> outboundContext)
            throws Exception {
        logger.debug("Attempting to sign the outbound SAML message...");
        final SAMLOutboundProtocolMessageSigningHandler handler = new SAMLOutboundProtocolMessageSigningHandler();
        handler.setSignErrorResponses(this.signErrorResponse);
        handler.invoke(outboundContext);
        logger.debug("Signed SAML message successfully");
    }

    /**
     * Prepare saml outbound destination handler.
     *
     * @param <T>             the type parameter
     * @param outboundContext the outbound context
     * @throws Exception the exception
     */
    protected <T extends SAMLObject> void prepareSamlOutboundDestinationHandler(final MessageContext<T> outboundContext)
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
    protected <T extends SAMLObject> void prepareSecurityParametersContext(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
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
     * @throws SamlException the saml exception
     */
    protected <T extends SAMLObject> void prepareOutboundContext(final T samlObject,
                                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                 final MessageContext<T> outboundContext) throws SamlException {

        logger.debug("Outbound saml object to use is [{}]", samlObject.getClass().getName());
        outboundContext.setMessage(samlObject);
        SamlIdPUtils.preparePeerEntitySamlEndpointContext(outboundContext, adaptor);
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
            logger.debug("Resolving signature signing parameters for [{}]", descriptor.getElementQName().getLocalPart());

            final SignatureSigningParameters params = resolver.resolveSingle(criteria);
            if (params == null) {
                throw new SAMLException("No signature signing parameter is available");
            }

            logger.debug("Created signature signing parameters."
                            + "\nSignature algorithm: [{}]"
                            + "\nSignature canonicalization algorithm: [{}]"
                            + "\nSignature reference digest methods: [{}]",
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
        logger.debug("Signature signing blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        logger.debug("Signature signing signature algorithms: [{}]", config.getSignatureAlgorithms());
        logger.debug("Signature signing signature canonicalization algorithm: [{}]", config.getSignatureCanonicalizationAlgorithm());
        logger.debug("Signature signing whitelisted algorithms: {}", config.getWhitelistedAlgorithms());
        logger.debug("Signature signing reference digest methods: [{}]", config.getSignatureReferenceDigestMethods());

        final PrivateKey privateKey = getSigningPrivateKey();
        final X509Certificate certificate = getSigningCertificate();

        final List<Credential> creds = new ArrayList<>();
        creds.add(new BasicX509Credential(certificate, privateKey));
        config.setSigningCredentials(creds);
        logger.debug("Signature signing credentials configured");

        return config;
    }

    /**
     * Gets signing certificate.
     *
     * @return the signing certificate
     */
    protected X509Certificate getSigningCertificate() {
        logger.debug("Locating signature signing certificate file from [{}]", this.signingCertFile);
        return SamlIdPUtils.readCertificate(new FileSystemResource(this.signingCertFile));
    }

    /**
     * Gets signing private key.
     *
     * @return the signing private key
     * @throws Exception the exception
     */
    protected PrivateKey getSigningPrivateKey() throws Exception {
        final PrivateKeyFactoryBean privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(new FileSystemResource(this.signingKeyFile));
        privateKeyFactoryBean.setAlgorithm(this.privateKeyAlgName);
        privateKeyFactoryBean.setSingleton(false);
        logger.debug("Locating signature signing key file from [{}]", this.signingKeyFile);
        return privateKeyFactoryBean.getObject();
    }

    /**
     * Validate authn request signature.
     *
     * @param profileRequest    the authn request
     * @param metadataResolver  the metadata resolver
     * @throws Exception the exception
     */
    public void verifySamlProfileRequestIfNeeded(final RequestAbstractType profileRequest,
                                                 final MetadataResolver metadataResolver)
            throws Exception {

        logger.debug("Validating signature of the request for [{}]", profileRequest.getClass().getName());
        final Signature signature = profileRequest.getSignature();
        if (signature == null) {
            throw new SAMLException("Request is signed but there is no signature associated with the request");
        }

        logger.debug("Validating profile signature...");
        final SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();
        validator.validate(signature);


        final MetadataCredentialResolver kekCredentialResolver = new MetadataCredentialResolver();
        final BasicRoleDescriptorResolver roleDescriptorResolver = new BasicRoleDescriptorResolver(metadataResolver);
        roleDescriptorResolver.initialize();

        final BasicSignatureValidationConfiguration config =
                DefaultSecurityConfigurationBootstrap.buildDefaultSignatureValidationConfiguration();

        kekCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        kekCredentialResolver.setKeyInfoCredentialResolver(
                DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        kekCredentialResolver.initialize();

        final CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new SignatureValidationConfigurationCriterion(config));
        criteriaSet.add(new EntityIdCriterion(profileRequest.getIssuer().getValue()));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));

        final Credential credential = kekCredentialResolver.resolveSingle(criteriaSet);
        if (credential == null) {
            throw new SamlException("Signing credential for validation could not be resolved");
        }

        logger.debug("Validating signature using credentials for [{}]", credential.getEntityId());
        SignatureValidator.validate(signature, credential);
        logger.info("Successfully validated the request signature.");
    }


}
