package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPResponseProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;

import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
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
import org.opensaml.saml.saml2.core.RequestAbstractType;
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
import org.springframework.core.io.FileSystemResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdPObjectSigner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlIdPObjectSigner {
    /**
     * The Override signature reference digest methods.
     */
    protected final List overrideSignatureReferenceDigestMethods;

    /**
     * The Override signature algorithms.
     */
    protected final List overrideSignatureAlgorithms;

    /**
     * The Override black listed signature algorithms.
     */
    protected final List overrideBlackListedSignatureAlgorithms;

    /**
     * The Override white listed signature signing algorithms.
     */
    protected final List overrideWhiteListedAlgorithms;

    private final MetadataResolver casSamlIdPMetadataResolver;

    private final CasConfigurationProperties casProperties;

    private final SamlIdPMetadataLocator samlIdPMetadataLocator;

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param <T>          the type parameter
     * @param samlObject   the saml object
     * @param service      the service
     * @param adaptor      the adaptor
     * @param response     the response
     * @param request      the request
     * @param binding      the binding
     * @param authnRequest the authn request
     * @return the t
     * @throws SamlException the saml exception
     */
    @SneakyThrows
    public <T extends SAMLObject> T encode(final T samlObject,
                                           final SamlRegisteredService service,
                                           final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                           final HttpServletResponse response,
                                           final HttpServletRequest request,
                                           final String binding,
                                           final RequestAbstractType authnRequest) throws SamlException {

        LOGGER.debug("Attempting to encode [{}] for [{}]", samlObject.getClass().getName(), adaptor.getEntityId());
        val outboundContext = new MessageContext<T>();
        prepareOutboundContext(samlObject, adaptor, outboundContext, binding, authnRequest);
        prepareSecurityParametersContext(adaptor, outboundContext, service);
        prepareEndpointURLSchemeSecurityHandler(outboundContext);
        prepareSamlOutboundDestinationHandler(outboundContext);
        prepareSamlOutboundProtocolMessageSigningHandler(outboundContext);
        return samlObject;

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
        val handler = new SAMLOutboundProtocolMessageSigningHandler();
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
        val handlerDest = new SAMLOutboundDestinationHandler();
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
        val handlerEnd = new EndpointURLSchemeSecurityHandler();
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
        @NonNull
        val secParametersContext = outboundContext.getSubcontext(SecurityParametersContext.class, true);
        val roleDesc = adaptor.getSsoDescriptor();
        val signingParameters = buildSignatureSigningParameters(roleDesc, service);
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
     * @param authnRequest    the authn request
     * @throws SamlException the saml exception
     */
    protected <T extends SAMLObject> void prepareOutboundContext(final T samlObject,
                                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                 final MessageContext<T> outboundContext,
                                                                 final String binding,
                                                                 final RequestAbstractType authnRequest) throws SamlException {

        LOGGER.debug("Outbound saml object to use is [{}]", samlObject.getClass().getName());
        outboundContext.setMessage(samlObject);
        SamlIdPUtils.preparePeerEntitySamlEndpointContext(authnRequest, outboundContext, adaptor, binding);
    }

    /**
     * Build signature signing parameters signature signing parameters.
     *
     * @param descriptor the descriptor
     * @param service    the service
     * @return the signature signing parameters
     */
    @SneakyThrows
    protected SignatureSigningParameters buildSignatureSigningParameters(final RoleDescriptor descriptor,
                                                                         final SamlRegisteredService service) {
        val criteria = new CriteriaSet();
        val signatureSigningConfiguration = getSignatureSigningConfiguration(descriptor, service);
        criteria.add(new SignatureSigningConfigurationCriterion(signatureSigningConfiguration));
        criteria.add(new RoleDescriptorCriterion(descriptor));
        val resolver = new SAMLMetadataSignatureSigningParametersResolver();
        LOGGER.debug("Resolving signature signing parameters for [{}]", descriptor.getElementQName().getLocalPart());
        @NonNull
        val params = resolver.resolveSingle(criteria);
        LOGGER.debug("Created signature signing parameters."
                + "\nSignature algorithm: [{}]"
                + "\nSignature canonicalization algorithm: [{}]"
                + "\nSignature reference digest methods: [{}]",
            params.getSignatureAlgorithm(),
            params.getSignatureCanonicalizationAlgorithm(),
            params.getSignatureReferenceDigestMethod());

        return params;

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
        val config =
            DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
        val samlIdp = casProperties.getAuthn().getSamlIdp();

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

        val privateKey = getSigningPrivateKey();
        val idp = casProperties.getAuthn().getSamlIdp();

        val kekCredentialResolver = new MetadataCredentialResolver();
        val roleDescriptorResolver = SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver, idp.getMetadata().isRequireValidMetadata());
        kekCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        kekCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        kekCredentialResolver.initialize();
        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new SignatureSigningConfigurationCriterion(config));
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
        criteriaSet.add(new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getEntityId()));
        criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val credentials = Sets.<Credential>newLinkedHashSet(kekCredentialResolver.resolve(criteriaSet));
        val creds = new ArrayList<Credential>();

        credentials.forEach(c -> {
            val cred = getResolvedSigningCredential(c, privateKey, service);
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
        val samlIdp = casProperties.getAuthn().getSamlIdp();

        try {
            val credType = SamlIdPResponseProperties.SignatureCredentialTypes.valueOf(
                StringUtils.defaultIfBlank(service.getSigningCredentialType(), samlIdp.getResponse().getCredentialType().name()).toUpperCase());
            LOGGER.debug("Requested credential type [{}] is found for service [{}]", credType, service);

            switch (credType) {
                case BASIC:
                    LOGGER.debug("Building basic credential signing key [{}] based on requested credential type", credType);
                    return new BasicCredential(c.getPublicKey(), privateKey);
                case X509:
                default:
                    if (c instanceof BasicX509Credential) {
                        val certificate = BasicX509Credential.class.cast(c).getEntityCertificate();
                        LOGGER.debug("Locating signature signing certificate from credential [{}]", CertUtils.toString(certificate));
                        return new BasicX509Credential(certificate, privateKey);
                    }
                    val signingCert = samlIdPMetadataLocator.getSigningCertificate();
                    LOGGER.debug("Locating signature signing certificate file from [{}]", signingCert);
                    val certificate = SamlUtils.readCertificate(signingCert);
                    return new BasicX509Credential(certificate, privateKey);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets signing private key.
     *
     * @return the signing private key
     * @throws Exception the exception
     */
    protected PrivateKey getSigningPrivateKey() throws Exception {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val signingKey = samlIdPMetadataLocator.getSigningKey();
        val privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(new FileSystemResource(signingKey.getFile()));
        privateKeyFactoryBean.setAlgorithm(samlIdp.getMetadata().getPrivateKeyAlgName());
        privateKeyFactoryBean.setSingleton(false);
        LOGGER.debug("Locating signature signing key file from [{}]", signingKey);
        return privateKeyFactoryBean.getObject();
    }
}
