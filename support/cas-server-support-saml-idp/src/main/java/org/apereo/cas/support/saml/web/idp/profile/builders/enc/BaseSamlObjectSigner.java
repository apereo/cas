package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import com.google.common.collect.Sets;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
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
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.AbstractCredential;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This is {@link BaseSamlObjectSigner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class BaseSamlObjectSigner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSamlObjectSigner.class);

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

    private final MetadataResolver casSamlIdPMetadataResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    public BaseSamlObjectSigner(final List overrideSignatureReferenceDigestMethods, final List overrideSignatureAlgorithms,
                                final List overrideBlackListedSignatureAlgorithms, final List overrideWhiteListedAlgorithms,
                                final MetadataResolver casSamlIdPMetadataResolver) {
        this.overrideSignatureReferenceDigestMethods = overrideSignatureReferenceDigestMethods;
        this.overrideSignatureAlgorithms = overrideSignatureAlgorithms;
        this.overrideBlackListedSignatureAlgorithms = overrideBlackListedSignatureAlgorithms;
        this.overrideWhiteListedAlgorithms = overrideWhiteListedAlgorithms;
        this.casSamlIdPMetadataResolver = casSamlIdPMetadataResolver;
    }

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param <T>        the type parameter
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @param response   the response
     * @param request    the request
     * @param binding    the binding
     * @return the t
     * @throws SamlException the saml exception
     */
    public <T extends SAMLObject> T encode(final T samlObject,
                                           final SamlRegisteredService service,
                                           final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                           final HttpServletResponse response,
                                           final HttpServletRequest request,
                                           final String binding) throws SamlException {
        try {
            LOGGER.debug("Attempting to encode [{}] for [{}]", samlObject.getClass().getName(), adaptor.getEntityId());
            final MessageContext<T> outboundContext = new MessageContext<>();
            prepareOutboundContext(samlObject, adaptor, outboundContext, binding);
            prepareSecurityParametersContext(adaptor, outboundContext, service);
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
    protected <T extends SAMLObject> void prepareSamlOutboundProtocolMessageSigningHandler(final MessageContext<T> outboundContext) throws Exception {
        LOGGER.debug("Attempting to sign the outbound SAML message...");
        final SAMLOutboundProtocolMessageSigningHandler handler = new SAMLOutboundProtocolMessageSigningHandler();
        handler.setSignErrorResponses(casProperties.getAuthn().getSamlIdp().getResponse().isSignError());
        handler.invoke(outboundContext);
        LOGGER.debug("Signed SAML message successfully");
    }

    /**
     * Prepare saml outbound destination handler.
     *
     * @param <T>             the type parameter
     * @param outboundContext the outbound context
     * @throws Exception the exception
     */
    protected <T extends SAMLObject> void prepareSamlOutboundDestinationHandler(final MessageContext<T> outboundContext) throws Exception {
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
    protected <T extends SAMLObject> void prepareEndpointURLSchemeSecurityHandler(final MessageContext<T> outboundContext) throws Exception {
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
     * @param service         the service
     * @throws SAMLException the saml exception
     */
    protected <T extends SAMLObject> void prepareSecurityParametersContext(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                           final MessageContext<T> outboundContext,
                                                                           final SamlRegisteredService service) throws SAMLException {
        final SecurityParametersContext secParametersContext = outboundContext.getSubcontext(SecurityParametersContext.class, true);
        if (secParametersContext == null) {
            throw new IllegalArgumentException("No signature signing parameters could be determined");
        }
        final RoleDescriptor roleDesc = adaptor.getSsoDescriptor();
        final SignatureSigningParameters signingParameters = buildSignatureSigningParameters(roleDesc, service);
        secParametersContext.setSignatureSigningParameters(signingParameters);
    }

    /**
     * Prepare outbound context.
     *
     * @param <T>             the type parameter
     * @param samlObject      the saml object
     * @param adaptor         the adaptor
     * @param outboundContext the outbound context
     * @param binding         the binding
     * @throws SamlException the saml exception
     */
    protected <T extends SAMLObject> void prepareOutboundContext(final T samlObject,
                                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                 final MessageContext<T> outboundContext,
                                                                 final String binding) throws SamlException {

        LOGGER.debug("Outbound saml object to use is [{}]", samlObject.getClass().getName());
        outboundContext.setMessage(samlObject);
        SamlIdPUtils.preparePeerEntitySamlEndpointContext(outboundContext, adaptor, binding);
    }

    /**
     * Build signature signing parameters signature signing parameters.
     *
     * @param descriptor the descriptor
     * @param service    the service
     * @return the signature signing parameters
     * @throws SAMLException the saml exception
     */
    protected SignatureSigningParameters buildSignatureSigningParameters(final RoleDescriptor descriptor,
                                                                         final SamlRegisteredService service) throws SAMLException {
        try {
            final CriteriaSet criteria = new CriteriaSet();
            final SignatureSigningConfiguration signatureSigningConfiguration = getSignatureSigningConfiguration(descriptor, service);
            criteria.add(new SignatureSigningConfigurationCriterion(signatureSigningConfiguration));
            criteria.add(new RoleDescriptorCriterion(descriptor));
            final SAMLMetadataSignatureSigningParametersResolver resolver = new SAMLMetadataSignatureSigningParametersResolver();
            LOGGER.debug("Resolving signature signing parameters for [{}]", descriptor.getElementQName().getLocalPart());

            final SignatureSigningParameters params = resolver.resolveSingle(criteria);
            if (params == null) {
                throw new SAMLException("No signature signing parameter is available");
            }

            LOGGER.debug("Created signature signing parameters."
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
     * @param roleDescriptor the role descriptor
     * @param service        the service
     * @return the signature signing configuration
     * @throws Exception the exception
     */
    protected SignatureSigningConfiguration getSignatureSigningConfiguration(final RoleDescriptor roleDescriptor,
                                                                             final SamlRegisteredService service) throws Exception {
        final BasicSignatureSigningConfiguration config =
                DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();

        if (this.overrideBlackListedSignatureAlgorithms != null
                && !samlIdp.getAlgs().getOverrideBlackListedSignatureSigningAlgorithms().isEmpty()) {
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

        if (StringUtils.isNotBlank(samlIdp.getAlgs().getOverrideSignatureCanonicalizationAlgorithm())) {
            config.setSignatureCanonicalizationAlgorithm(samlIdp.getAlgs().getOverrideSignatureCanonicalizationAlgorithm());
        }
        LOGGER.debug("Signature signing blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.debug("Signature signing signature algorithms: [{}]", config.getSignatureAlgorithms());
        LOGGER.debug("Signature signing signature canonicalization algorithm: [{}]", config.getSignatureCanonicalizationAlgorithm());
        LOGGER.debug("Signature signing whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());
        LOGGER.debug("Signature signing reference digest methods: [{}]", config.getSignatureReferenceDigestMethods());

        final PrivateKey privateKey = getSigningPrivateKey();
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();

        final MetadataCredentialResolver kekCredentialResolver = new MetadataCredentialResolver();
        kekCredentialResolver.setRoleDescriptorResolver(SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver,
                idp.getMetadata().isRequireValidMetadata()));
        kekCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        kekCredentialResolver.initialize();
        final CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new SignatureSigningConfigurationCriterion(config));
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
        criteriaSet.add(new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getEntityId()));
        criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));

        final Set<Credential> credentials = Sets.newLinkedHashSet(kekCredentialResolver.resolve(criteriaSet));
        final List<Credential> creds = new ArrayList<>();

        credentials.forEach(c -> {
            final AbstractCredential cred = getResolvedSigningCredential(c, privateKey, service);
            if (cred != null) {
                creds.add(cred);
            }
        });

        config.setSigningCredentials(creds);
        LOGGER.debug("Signature signing credentials configured with [{}] credentials", creds.size());

        return config;
    }

    private AbstractCredential getResolvedSigningCredential(final Credential c, final PrivateKey privateKey,
                                                            final SamlRegisteredService service) {
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();

        try {
            final SamlIdPProperties.Response.SignatureCredentialTypes credType = SamlIdPProperties.Response.SignatureCredentialTypes.valueOf(
                    StringUtils.defaultIfBlank(service.getSigningCredentialType(), samlIdp.getResponse().getCredentialType().name()).toUpperCase());
            LOGGER.debug("Requested credential type [{}] is found for service [{}]", credType, service);

            switch (credType) {
                case BASIC:
                    LOGGER.debug("Building basic credential signing key [{}] based on requested credential type", credType);
                    return new BasicCredential(c.getPublicKey(), privateKey);
                case X509:
                default:
                    if (c instanceof BasicX509Credential) {
                        final X509Certificate certificate = BasicX509Credential.class.cast(c).getEntityCertificate();
                        LOGGER.debug("Locating signature signing certificate from credential [{}]", CertUtils.toString(certificate));
                        return new BasicX509Credential(certificate, privateKey);
                    }
                    final Resource signingCert = samlIdp.getMetadata().getSigningCertFile();
                    LOGGER.debug("Locating signature signing certificate file from [{}]", signingCert);
                    final X509Certificate certificate = SamlUtils.readCertificate(signingCert);
                    return new BasicX509Credential(certificate, privateKey);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets signing certificate.
     *
     * @return the signing certificate
     * @throws Exception the exception
     */
    protected X509Certificate getSigningCertificate() throws Exception {
        final SamlIdPProperties samlIdp = casProperties.getAuthn().getSamlIdp();
        LOGGER.debug("Locating signature signing certificate file from [{}]", samlIdp.getMetadata().getSigningCertFile());
        return SamlUtils.readCertificate(new FileSystemResource(samlIdp.getMetadata().getSigningCertFile().getFile()));
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
        privateKeyFactoryBean.setLocation(new FileSystemResource(samlIdp.getMetadata().getSigningKeyFile().getFile()));
        privateKeyFactoryBean.setAlgorithm(samlIdp.getMetadata().getPrivateKeyAlgName());
        privateKeyFactoryBean.setSingleton(false);
        LOGGER.debug("Locating signature signing key file from [{}]", samlIdp.getMetadata().getSigningKeyFile());
        return privateKeyFactoryBean.getObject();
    }
}
