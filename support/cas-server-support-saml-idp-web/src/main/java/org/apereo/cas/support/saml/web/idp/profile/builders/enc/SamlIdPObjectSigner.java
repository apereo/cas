package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPResponseProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.crypto.CertUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.messaging.context.MessageContext;
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
import org.opensaml.security.credential.MutableCredential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.WhitelistBlacklistConfiguration;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link SamlIdPObjectSigner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlIdPObjectSigner {
    private final MetadataResolver casSamlIdPMetadataResolver;

    private final CasConfigurationProperties casProperties;

    private final SamlIdPMetadataLocator samlIdPMetadataLocator;

    private static boolean doesCredentialFingerprintMatch(final AbstractCredential credential, final SamlRegisteredService samlRegisteredService) {
        val fingerprint = samlRegisteredService.getSigningCredentialFingerprint();
        if (StringUtils.isNotBlank(fingerprint)) {
            val digest = DigestUtils.digest("SHA-1", Objects.requireNonNull(credential.getPublicKey()).getEncoded());
            val pattern = RegexUtils.createPattern(fingerprint, Pattern.CASE_INSENSITIVE);
            LOGGER.debug("Matching credential fingerprint [{}] against filter [{}] for service [{}]", digest, fingerprint, samlRegisteredService.getName());
            return pattern.matcher(digest).find();
        }
        return true;
    }

    private static AbstractCredential finalizeSigningCredential(final MutableCredential credential, final Credential original) {
        credential.setEntityId(original.getEntityId());
        credential.setUsageType(original.getUsageType());
        Objects.requireNonNull(original.getCredentialContextSet())
            .forEach(ctx -> Objects.requireNonNull(credential.getCredentialContextSet()).add(ctx));
        return (AbstractCredential) credential;
    }

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

        LOGGER.trace("Attempting to encode [{}] for [{}]", samlObject.getClass().getName(), adaptor.getEntityId());
        val outboundContext = new MessageContext();
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
    protected <T extends SAMLObject> void prepareSamlOutboundProtocolMessageSigningHandler(final MessageContext outboundContext) throws Exception {
        LOGGER.trace("Attempting to sign the outbound SAML message...");
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
    protected <T extends SAMLObject> void prepareSamlOutboundDestinationHandler(final MessageContext outboundContext) throws Exception {
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
    protected <T extends SAMLObject> void prepareEndpointURLSchemeSecurityHandler(final MessageContext outboundContext) throws Exception {
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
     */
    protected <T extends SAMLObject> void prepareSecurityParametersContext(final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                           final MessageContext outboundContext,
                                                                           final SamlRegisteredService service) {
        val secParametersContext = outboundContext.getSubcontext(SecurityParametersContext.class, true);
        val roleDesc = adaptor.getSsoDescriptor();
        val signingParameters = buildSignatureSigningParameters(roleDesc, service);
        Objects.requireNonNull(secParametersContext).setSignatureSigningParameters(signingParameters);
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
                                                                 final MessageContext outboundContext,
                                                                 final String binding,
                                                                 final RequestAbstractType authnRequest) throws SamlException {

        LOGGER.trace("Outbound saml object to use is [{}]", samlObject.getClass().getName());
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
        val signatureSigningConfiguration = getSignatureSigningConfiguration(service);
        criteria.add(new SignatureSigningConfigurationCriterion(signatureSigningConfiguration));
        criteria.add(new RoleDescriptorCriterion(descriptor));
        val resolver = new SAMLMetadataSignatureSigningParametersResolver();
        LOGGER.trace("Resolving signature signing parameters for [{}]", descriptor.getElementQName().getLocalPart());
        val params = resolver.resolveSingle(criteria);
        if (params != null) {
            LOGGER.trace("Created signature signing parameters."
                    + "\nSignature algorithm: [{}]"
                    + "\nSignature canonicalization algorithm: [{}]"
                    + "\nSignature reference digest methods: [{}]"
                    + "\nSignature reference canonicalization algorithm: [{}]",
                params.getSignatureAlgorithm(),
                params.getSignatureCanonicalizationAlgorithm(),
                params.getSignatureReferenceDigestMethod(),
                params.getSignatureReferenceCanonicalizationAlgorithm());
        } else {
            LOGGER.warn("Unable to resolve SignatureSigningParameters, response signing will fail."
                + " Make sure domain names in IDP metadata URLs and certificates match CAS domain name");
        }
        return params;
    }

    /**
     * Gets signature signing configuration.
     *
     * @param service the service
     * @return the signature signing configuration
     * @throws Exception the exception
     */
    protected SignatureSigningConfiguration getSignatureSigningConfiguration(final SamlRegisteredService service) throws Exception {
        val config = configureSignatureSigningSecurityConfiguration(service);

        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val privateKey = getSigningPrivateKey(service);

        val kekCredentialResolver = new MetadataCredentialResolver();
        val roleDescriptorResolver = SamlIdPUtils.getRoleDescriptorResolver(casSamlIdPMetadataResolver, samlIdp.getMetadata().isRequireValidMetadata());
        kekCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        kekCredentialResolver.setKeyInfoCredentialResolver(DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver());
        kekCredentialResolver.initialize();

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new SignatureSigningConfigurationCriterion(config));
        criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
        criteriaSet.add(new EntityIdCriterion(samlIdp.getEntityId()));
        criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));

        val credentials = Sets.<Credential>newLinkedHashSet(kekCredentialResolver.resolve(criteriaSet));
        val creds = new ArrayList<Credential>(2);

        credentials.forEach(c -> {
            val cred = getResolvedSigningCredential(c, privateKey, service);
            if (cred != null) {
                if (doesCredentialFingerprintMatch(cred, service)) {
                    creds.add(cred);
                }
            }
        });

        if (creds.isEmpty()) {
            LOGGER.error("Unable to locate any signing credentials for service [{}]", service.getName());
            throw new IllegalArgumentException("Unable to locate signing credentials");
        }

        config.setSigningCredentials(creds);
        LOGGER.trace("Signature signing credentials configured with [{}] credentials", creds.size());
        return config;
    }

    private BasicSignatureSigningConfiguration configureSignatureSigningSecurityConfiguration(final SamlRegisteredService service) {
        val config = DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
        LOGGER.trace("Default signature signing blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.trace("Default signature signing signature algorithms: [{}]", config.getSignatureAlgorithms());
        LOGGER.trace("Default signature signing signature canonicalization algorithm: [{}]", config.getSignatureCanonicalizationAlgorithm());
        LOGGER.trace("Default signature signing whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());
        LOGGER.trace("Default signature signing reference digest methods: [{}]", config.getSignatureReferenceDigestMethods());

        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val globalAlgorithms = samlIdp.getAlgs();

        val overrideSignatureReferenceDigestMethods = service.getSigningSignatureReferenceDigestMethods().isEmpty()
            ? globalAlgorithms.getOverrideSignatureReferenceDigestMethods()
            : service.getSigningSignatureReferenceDigestMethods();
        if (overrideSignatureReferenceDigestMethods != null && !overrideSignatureReferenceDigestMethods.isEmpty()) {
            config.setSignatureReferenceDigestMethods(overrideSignatureReferenceDigestMethods);
        }

        val overrideSignatureAlgorithms = service.getSigningSignatureAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideSignatureAlgorithms()
            : service.getSigningSignatureAlgorithms();
        if (overrideSignatureAlgorithms != null && !overrideSignatureAlgorithms.isEmpty()) {
            config.setSignatureAlgorithms(overrideSignatureAlgorithms);
        }

        val overrideBlackListedSignatureAlgorithms = service.getSigningSignatureBlackListedAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideBlackListedSignatureSigningAlgorithms()
            : service.getSigningSignatureBlackListedAlgorithms();
        if (overrideBlackListedSignatureAlgorithms != null && !overrideBlackListedSignatureAlgorithms.isEmpty()) {
            config.setBlacklistedAlgorithms(overrideBlackListedSignatureAlgorithms);
        }

        val overrideWhiteListedAlgorithms = service.getSigningSignatureWhiteListedAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideWhiteListedSignatureSigningAlgorithms()
            : service.getSigningSignatureWhiteListedAlgorithms();
        if (overrideWhiteListedAlgorithms != null && !overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(overrideWhiteListedAlgorithms);
        }

        if (StringUtils.isNotBlank(service.getSigningSignatureCanonicalizationAlgorithm())) {
            config.setSignatureCanonicalizationAlgorithm(service.getSigningSignatureCanonicalizationAlgorithm());
        } else if (StringUtils.isNotBlank(globalAlgorithms.getOverrideSignatureCanonicalizationAlgorithm())) {
            config.setSignatureCanonicalizationAlgorithm(globalAlgorithms.getOverrideSignatureCanonicalizationAlgorithm());
        }
        LOGGER.trace("Finalized signature signing blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.trace("Finalized signature signing signature algorithms: [{}]", config.getSignatureAlgorithms());
        LOGGER.trace("Finalized signature signing signature canonicalization algorithm: [{}]", config.getSignatureCanonicalizationAlgorithm());
        LOGGER.trace("Finalized signature signing whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());
        LOGGER.trace("Finalized signature signing reference digest methods: [{}]", config.getSignatureReferenceDigestMethods());

        if (StringUtils.isNotBlank(service.getWhiteListBlackListPrecedence())) {
            val precedence = WhitelistBlacklistConfiguration.Precedence.valueOf(service.getWhiteListBlackListPrecedence().trim().toUpperCase());
            config.setWhitelistBlacklistPrecedence(precedence);
        }
        return config;
    }

    private AbstractCredential getResolvedSigningCredential(final Credential c, final PrivateKey privateKey,
                                                            final SamlRegisteredService service) {
        try {
            val samlIdp = casProperties.getAuthn().getSamlIdp();
            val credType = SamlIdPResponseProperties.SignatureCredentialTypes.valueOf(
                StringUtils.defaultIfBlank(service.getSigningCredentialType(), samlIdp.getResponse().getCredentialType().name()).toUpperCase());
            LOGGER.trace("Requested credential type [{}] is found for service [{}]", credType, service.getName());

            switch (credType) {
                case BASIC:
                    LOGGER.debug("Building credential signing key [{}] based on requested credential type", credType);
                    if (c.getPublicKey() == null) {
                        throw new IllegalArgumentException("Unable to identify the public key from the signing credential");
                    }
                    return finalizeSigningCredential(new BasicCredential(c.getPublicKey(), privateKey), c);
                case X509:
                default:
                    if (c instanceof BasicX509Credential) {
                        val certificate = BasicX509Credential.class.cast(c).getEntityCertificate();
                        LOGGER.debug("Locating signature signing certificate from credential [{}]", CertUtils.toString(certificate));
                        return finalizeSigningCredential(new BasicX509Credential(certificate, privateKey), c);
                    }
                    val signingCert = samlIdPMetadataLocator.resolveSigningCertificate(Optional.of(service));
                    LOGGER.debug("Locating signature signing certificate file from [{}]", signingCert);
                    val certificate = SamlUtils.readCertificate(signingCert);
                    return finalizeSigningCredential(new BasicX509Credential(certificate, privateKey), c);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets signing private key.
     *
     * @param registeredService the registered service
     * @return the signing private key
     * @throws Exception the exception
     */
    protected PrivateKey getSigningPrivateKey(final SamlRegisteredService registeredService) throws Exception {
        val samlIdp = casProperties.getAuthn().getSamlIdp();
        val signingKey = samlIdPMetadataLocator.resolveSigningKey(Optional.of(registeredService));
        val privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(signingKey);
        if (StringUtils.isBlank(registeredService.getSigningKeyAlgorithm())) {
            privateKeyFactoryBean.setAlgorithm(samlIdp.getMetadata().getPrivateKeyAlgName());
        } else {
            privateKeyFactoryBean.setAlgorithm(registeredService.getSigningKeyAlgorithm());
        }
        privateKeyFactoryBean.setSingleton(false);
        LOGGER.debug("Locating signature signing key for [{}] using algorithm [{}}",
            registeredService, privateKeyFactoryBean.getAlgorithm());
        return privateKeyFactoryBean.getObject();
    }
}
