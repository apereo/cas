package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import com.google.common.base.Throwables;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.binding.impl.SAMLOutboundDestinationHandler;
import org.opensaml.saml.common.binding.security.impl.EndpointURLSchemeSecurityHandler;
import org.opensaml.saml.common.binding.security.impl.SAMLOutboundProtocolMessageSigningHandler;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.RoleDescriptorResolver;
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver;
import org.opensaml.saml.saml2.binding.security.impl.SAML2HTTPRedirectDeflateSignatureSecurityHandler;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.CredentialResolver;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.SignatureValidationConfiguration;
import org.opensaml.xmlsec.SignatureValidationParameters;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.criterion.SignatureValidationConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.impl.BasicSignatureValidationConfiguration;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureTrustEngine;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class SamlObjectSigner {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Override signature reference digest methods.
     */
    protected List overrideSignatureReferenceDigestMethods;

    /**
     * The Override signature algorithms.
     */
    protected List overrideSignatureAlgorithms;

    /**
     * The Override black listed signature algorithms.
     */
    protected List overrideBlackListedSignatureAlgorithms;

    /**
     * The Override white listed signature signing algorithms.
     */
    protected List overrideWhiteListedAlgorithms;


    @Autowired
    private CasConfigurationProperties casProperties;

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
        handler.setSignErrorResponses(casProperties.getAuthn().getSamlIdp().getResponse().isSignError());
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
     * Gets signature validation configuration.
     *
     * @return the signature validation configuration
     * @throws Exception the exception
     */
    protected SignatureValidationConfiguration getSignatureValidationConfiguration() throws Exception {
        final BasicSignatureValidationConfiguration config =
                DefaultSecurityConfigurationBootstrap.buildDefaultSignatureValidationConfiguration();
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();

        if (this.overrideBlackListedSignatureAlgorithms != null
                && !samlIdp.getResponse().getOverrideSignatureCanonicalizationAlgorithm().isEmpty()) {
            config.setBlacklistedAlgorithms(this.overrideBlackListedSignatureAlgorithms);
            config.setWhitelistMerge(true);
        }

        if (this.overrideWhiteListedAlgorithms != null && !this.overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(this.overrideWhiteListedAlgorithms);
            config.setBlacklistMerge(true);
        }

        logger.debug("Signature validation blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        logger.debug("Signature validation whitelisted algorithms: {}", config.getWhitelistedAlgorithms());

        return config;
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
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();

        if (this.overrideBlackListedSignatureAlgorithms != null
                && !samlIdp.getResponse().getOverrideSignatureCanonicalizationAlgorithm().isEmpty()) {
            config.setBlacklistedAlgorithms(this.overrideBlackListedSignatureAlgorithms);
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

        if (StringUtils.isNotBlank(samlIdp.getResponse().getOverrideSignatureCanonicalizationAlgorithm())) {
            config.setSignatureCanonicalizationAlgorithm(samlIdp.getResponse().getOverrideSignatureCanonicalizationAlgorithm());
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
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();
        logger.debug("Locating signature signing certificate file from [{}]", samlIdp.getMetadata().getSigningCertFile());
        return SamlUtils.readCertificate(new FileSystemResource(samlIdp.getMetadata().getSigningCertFile()));
    }

    /**
     * Gets signing private key.
     *
     * @return the signing private key
     * @throws Exception the exception
     */
    protected PrivateKey getSigningPrivateKey() throws Exception {
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();
        final PrivateKeyFactoryBean privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(new FileSystemResource(samlIdp.getMetadata().getSigningKeyFile()));
        privateKeyFactoryBean.setAlgorithm(samlIdp.getMetadata().getPrivateKeyAlgName());
        privateKeyFactoryBean.setSingleton(false);
        logger.debug("Locating signature signing key file from [{}]", samlIdp.getMetadata().getSigningKeyFile());
        return privateKeyFactoryBean.getObject();
    }

    /**
     * Validate authn request signature.
     *
     * @param profileRequest   the authn request
     * @param metadataResolver the metadata resolver
     * @param request          the request
     * @param context          the context
     * @throws Exception the exception
     */
    public void verifySamlProfileRequestIfNeeded(final RequestAbstractType profileRequest,
                                                 final MetadataResolver metadataResolver,
                                                 final HttpServletRequest request,
                                                 final MessageContext context) throws Exception {


        final BasicRoleDescriptorResolver roleDescriptorResolver = new BasicRoleDescriptorResolver(metadataResolver);
        roleDescriptorResolver.initialize();

        logger.debug("Validating signature for [{}]", profileRequest.getClass().getName());

        final Signature signature = profileRequest.getSignature();
        if (signature != null) {
            final SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();
            logger.debug("Validating profile signature for {} via {}...", profileRequest.getIssuer(), validator.getClass().getSimpleName());
            validator.validate(signature);
            logger.debug("Successfully validated profile signature for {}.", profileRequest.getIssuer());

            final Credential credential = getSigningCredential(roleDescriptorResolver, profileRequest);
            if (credential == null) {
                throw new SamlException("Signing credential for validation could not be resolved");
            }

            logger.debug("Validating signature using credentials for [{}]", credential.getEntityId());
            SignatureValidator.validate(signature, credential);
            logger.info("Successfully validated the request signature.");

        } else {
            final SAML2HTTPRedirectDeflateSignatureSecurityHandler handler = new SAML2HTTPRedirectDeflateSignatureSecurityHandler();
            final SAMLPeerEntityContext peer = context.getSubcontext(SAMLPeerEntityContext.class, true);
            peer.setEntityId(SamlIdPUtils.getIssuerFromSamlRequest(profileRequest));
            logger.debug("Validating request signature for {} via {}...", peer.getEntityId(), handler.getClass().getSimpleName());

            logger.debug("Resolving role descriptor for {}", peer.getEntityId());

            final RoleDescriptor roleDescriptor = roleDescriptorResolver.resolveSingle(
                    new CriteriaSet(new EntityIdCriterion(peer.getEntityId()),
                            new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME)));
            peer.setRole(roleDescriptor.getElementQName());
            final SAMLProtocolContext protocol = context.getSubcontext(SAMLProtocolContext.class, true);
            protocol.setProtocol(SAMLConstants.SAML20P_NS);

            logger.debug("Building security parameters context for signature validation of {}", peer.getEntityId());
            final SecurityParametersContext secCtx = context.getSubcontext(SecurityParametersContext.class, true);
            final SignatureValidationParameters validationParams = new SignatureValidationParameters();

            if (overrideBlackListedSignatureAlgorithms != null && !overrideBlackListedSignatureAlgorithms.isEmpty()) {
                validationParams.setBlacklistedAlgorithms(this.overrideBlackListedSignatureAlgorithms);
                logger.debug("Validation override blacklisted algorithms are {}", this.overrideWhiteListedAlgorithms);
            }

            if (overrideWhiteListedAlgorithms != null && !overrideWhiteListedAlgorithms.isEmpty()) {
                validationParams.setWhitelistedAlgorithms(this.overrideWhiteListedAlgorithms);
                logger.debug("Validation override whitelisted algorithms are {}", this.overrideWhiteListedAlgorithms);
            }

            logger.debug("Resolving signing credentials for {}", peer.getEntityId());
            final Credential credential = getSigningCredential(roleDescriptorResolver, profileRequest);
            if (credential == null) {
                throw new SamlException("Signing credential for validation could not be resolved");
            }

            final CredentialResolver resolver = new StaticCredentialResolver(credential);
            final KeyInfoCredentialResolver keyResolver = new StaticKeyInfoCredentialResolver(credential);
            final SignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(resolver, keyResolver);
            validationParams.setSignatureTrustEngine(trustEngine);
            secCtx.setSignatureValidationParameters(validationParams);

            handler.setHttpServletRequest(request);
            logger.debug("Initializing {} to execute signature validation for {}", handler.getClass().getSimpleName(), peer.getEntityId());
            handler.initialize();

            logger.debug("Invoking {} to handle signature validation for {}", handler.getClass().getSimpleName(), peer.getEntityId());
            handler.invoke(context);
            logger.debug("Successfully validated request signature for {}.", profileRequest.getIssuer());
        }
    }

    public void setOverrideSignatureReferenceDigestMethods(final List overrideSignatureReferenceDigestMethods) {
        this.overrideSignatureReferenceDigestMethods = overrideSignatureReferenceDigestMethods;
    }

    public void setOverrideSignatureAlgorithms(final List overrideSignatureAlgorithms) {
        this.overrideSignatureAlgorithms = overrideSignatureAlgorithms;
    }

    public void setOverrideBlackListedSignatureAlgorithms(final List overrideBlackListedSignatureAlgorithms) {
        this.overrideBlackListedSignatureAlgorithms = overrideBlackListedSignatureAlgorithms;
    }

    public void setOverrideWhiteListedAlgorithms(final List overrideWhiteListedAlgorithms) {
        this.overrideWhiteListedAlgorithms = overrideWhiteListedAlgorithms;
    }

    private Credential getSigningCredential(final RoleDescriptorResolver resolver, final RequestAbstractType profileRequest) {
        try {
            final MetadataCredentialResolver kekCredentialResolver = new MetadataCredentialResolver();
            final SignatureValidationConfiguration config = getSignatureValidationConfiguration();
            kekCredentialResolver.setRoleDescriptorResolver(resolver);
            kekCredentialResolver.setKeyInfoCredentialResolver(
                    DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
            kekCredentialResolver.initialize();
            final CriteriaSet criteriaSet = new CriteriaSet();
            criteriaSet.add(new SignatureValidationConfigurationCriterion(config));
            criteriaSet.add(new EntityIdCriterion(SamlIdPUtils.getIssuerFromSamlRequest(profileRequest)));
            criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            criteriaSet.add(new UsageCriterion(UsageType.SIGNING));

            return kekCredentialResolver.resolveSingle(criteriaSet);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
