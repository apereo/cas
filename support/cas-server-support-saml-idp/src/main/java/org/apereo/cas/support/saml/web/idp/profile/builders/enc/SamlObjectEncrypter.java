package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.PrivateKeyFactoryBean;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.impl.BasicRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.criterion.EncryptionConfigurationCriterion;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.impl.BasicEncryptionConfiguration;
import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DEREncodedKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.KeyInfoReferenceProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
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
 * This is {@link SamlObjectEncrypter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlObjectEncrypter {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Override data encryption algorithms.
     */
    protected List overrideDataEncryptionAlgorithms;

    /**
     * The Override key encryption algorithms.
     */
    protected List overrideKeyEncryptionAlgorithms;

    /**
     * The Override black listed encryption signing algorithms.
     */
    protected List overrideBlackListedEncryptionAlgorithms;

    /**
     * The Override white listed encryption signing algorithms.
     */
    protected List overrideWhiteListedAlgorithms;

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @param response   the response
     * @param request    the request
     * @return the t
     * @throws SamlException the saml exception
     */
    public EncryptedAssertion encode(final Assertion samlObject,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final HttpServletResponse response,
                                     final HttpServletRequest request) throws SamlException {
        try {
            logger.debug("Attempting to encrypt [{}] for [{}]", samlObject.getClass().getName(), adaptor.getEntityId());
            final Credential credential = getKeyEncryptionCredential(adaptor.getEntityId(), adaptor, service);
            logger.info("Found encryption public key: [{}]", EncodingUtils.encodeBase64(credential.getPublicKey().getEncoded()));

            final KeyEncryptionParameters keyEncParams = getKeyEncryptionParameters(samlObject, service, adaptor, credential);
            logger.debug("Key encryption algorithm for [{}] is [{}]", keyEncParams.getRecipient(), keyEncParams.getAlgorithm());

            final DataEncryptionParameters dataEncParams = getDataEncryptionParameters(samlObject, service, adaptor);
            logger.debug("Data encryption algorithm for [{}] is [{}]", adaptor.getEntityId(), dataEncParams.getAlgorithm());

            final Encrypter encrypter = getEncrypter(samlObject, service, adaptor, keyEncParams, dataEncParams);
            logger.debug("Attempting to encrypt [{}] for [{}] with key placement of [{}]",
                    samlObject.getClass().getName(), adaptor.getEntityId(), encrypter.getKeyPlacement());

            return encrypter.encrypt(samlObject);
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    /**
     * Gets encrypter.
     *
     * @param samlObject    the saml object
     * @param service       the service
     * @param adaptor       the adaptor
     * @param keyEncParams  the key enc params
     * @param dataEncParams the data enc params
     * @return the encrypter
     */
    protected Encrypter getEncrypter(final Assertion samlObject, final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final KeyEncryptionParameters keyEncParams, final
                                     DataEncryptionParameters dataEncParams) {
        final Encrypter encrypter = new Encrypter(dataEncParams, keyEncParams);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.PEER);
        return encrypter;
    }

    /**
     * Gets data encryption parameters.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @return the data encryption parameters
     */
    protected DataEncryptionParameters getDataEncryptionParameters(final Assertion samlObject, final SamlRegisteredService service,
                                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        final DataEncryptionParameters dataEncParams = new DataEncryptionParameters();
        dataEncParams.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        return dataEncParams;
    }

    /**
     * Gets key encryption parameters.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @param credential the credential
     * @return the key encryption parameters
     */
    protected KeyEncryptionParameters getKeyEncryptionParameters(final Assertion samlObject, final SamlRegisteredService service,
                                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                 final Credential credential) {
        final KeyEncryptionParameters keyEncParams = new KeyEncryptionParameters();
        keyEncParams.setRecipient(adaptor.getEntityId());
        keyEncParams.setEncryptionCredential(credential);
        keyEncParams.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        return keyEncParams;
    }

    /**
     * Gets key encryption credential.
     *
     * @param peerEntityId the peer entity id
     * @param adaptor      the adaptor
     * @param service      the service
     * @return the key encryption credential
     * @throws Exception the exception
     */
    protected Credential getKeyEncryptionCredential(final String peerEntityId,
                                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                    final SamlRegisteredService service) throws Exception {
        final BasicEncryptionConfiguration config =
                DefaultSecurityConfigurationBootstrap.buildDefaultEncryptionConfiguration();

        if (this.overrideBlackListedEncryptionAlgorithms != null && !this.overrideBlackListedEncryptionAlgorithms.isEmpty()) {
            config.setBlacklistedAlgorithms(this.overrideBlackListedEncryptionAlgorithms);
        }

        if (this.overrideWhiteListedAlgorithms != null && !this.overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(this.overrideWhiteListedAlgorithms);
        }

        if (this.overrideDataEncryptionAlgorithms != null && !this.overrideDataEncryptionAlgorithms.isEmpty()) {
            config.setDataEncryptionAlgorithms(this.overrideDataEncryptionAlgorithms);
        }

        if (this.overrideKeyEncryptionAlgorithms != null && !this.overrideKeyEncryptionAlgorithms.isEmpty()) {
            config.setKeyTransportEncryptionAlgorithms(this.overrideKeyEncryptionAlgorithms);
        }

        logger.debug("Encryption blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        logger.debug("Encryption key algorithms: [{}]", config.getKeyTransportEncryptionAlgorithms());
        logger.debug("Signature data algorithms: [{}]", config.getDataEncryptionAlgorithms());
        logger.debug("Encryption whitelisted algorithms: {}", config.getWhitelistedAlgorithms());

        final MetadataCredentialResolver kekCredentialResolver = new MetadataCredentialResolver();

        final List<KeyInfoProvider> providers = new ArrayList<>();
        providers.add(new RSAKeyValueProvider());
        providers.add(new DSAKeyValueProvider());
        providers.add(new InlineX509DataProvider());
        providers.add(new DEREncodedKeyValueProvider());
        providers.add(new KeyInfoReferenceProvider());

        final BasicProviderKeyInfoCredentialResolver keyInfoResolver = new BasicProviderKeyInfoCredentialResolver(providers);
        kekCredentialResolver.setKeyInfoCredentialResolver(keyInfoResolver);

        final BasicRoleDescriptorResolver roleDescriptorResolver = new BasicRoleDescriptorResolver(adaptor.getMetadataResolver());
        roleDescriptorResolver.initialize();
        
        kekCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        kekCredentialResolver.initialize();

        final CriteriaSet criteriaSet = new CriteriaSet();
        criteriaSet.add(new EncryptionConfigurationCriterion(config));
        criteriaSet.add(new EntityIdCriterion(peerEntityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new UsageCriterion(UsageType.ENCRYPTION));

        logger.debug("Attempting to resolve the encryption key for entity id [{}]", peerEntityId);
        return kekCredentialResolver.resolveSingle(criteriaSet);
    }

    /**
     * Gets encryption certificate.
     *
     * @return the encryption certificate
     */
    protected X509Certificate getEncryptionCertificate() {
        logger.debug("Locating encryption certificate file from [{}]",
                casProperties.getAuthn().getSamlIdp().getMetadata().getEncryptionCertFile());
        return SamlUtils.readCertificate(new FileSystemResource(
                casProperties.getAuthn().getSamlIdp().getMetadata().getEncryptionCertFile()));
    }

    /**
     * Gets encryption private key.
     *
     * @return the encryption private key
     * @throws Exception the exception
     */
    protected PrivateKey getEncryptionPrivateKey() throws Exception {
        final PrivateKeyFactoryBean privateKeyFactoryBean = new PrivateKeyFactoryBean();
        privateKeyFactoryBean.setLocation(new FileSystemResource(casProperties.getAuthn().getSamlIdp().getMetadata().getEncryptionKeyFile()));
        privateKeyFactoryBean.setAlgorithm(casProperties.getAuthn().getSamlIdp().getMetadata().getPrivateKeyAlgName());
        privateKeyFactoryBean.setSingleton(false);
        logger.debug("Locating encryption key file from [{}]", casProperties.getAuthn().getSamlIdp().getMetadata().getEncryptionKeyFile());
        return privateKeyFactoryBean.getObject();
    }

    public void setOverrideDataEncryptionAlgorithms(final List overrideDataEncryptionAlgorithms) {
        this.overrideDataEncryptionAlgorithms = overrideDataEncryptionAlgorithms;
    }

    public void setOverrideKeyEncryptionAlgorithms(final List overrideKeyEncryptionAlgorithms) {
        this.overrideKeyEncryptionAlgorithms = overrideKeyEncryptionAlgorithms;
    }

    public void setOverrideBlackListedEncryptionAlgorithms(final List overrideBlackListedEncryptionAlgorithms) {
        this.overrideBlackListedEncryptionAlgorithms = overrideBlackListedEncryptionAlgorithms;
    }

    public void setOverrideWhiteListedAlgorithms(final List overrideWhiteListedAlgorithms) {
        this.overrideWhiteListedAlgorithms = overrideWhiteListedAlgorithms;
    }
}
