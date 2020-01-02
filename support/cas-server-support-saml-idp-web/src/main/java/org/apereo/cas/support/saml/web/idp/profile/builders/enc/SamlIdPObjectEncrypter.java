package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.EncryptedAttribute;
import org.opensaml.saml.saml2.core.EncryptedID;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.EncryptionParameters;
import org.opensaml.xmlsec.WhitelistBlacklistConfiguration;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.criterion.EncryptionConfigurationCriterion;
import org.opensaml.xmlsec.criterion.EncryptionOptionalCriterion;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.impl.BasicEncryptionConfiguration;
import org.opensaml.xmlsec.impl.BasicEncryptionParametersResolver;
import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DEREncodedKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.KeyInfoReferenceProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;

import java.util.ArrayList;

/**
 * This is {@link SamlIdPObjectEncrypter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlIdPObjectEncrypter {

    private final SamlIdPProperties samlIdPProperties;

    /**
     * Encode a given saml object by invoking a number of outbound security handlers on the context.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @return the t
     */
    @SneakyThrows
    public EncryptedAssertion encode(final Assertion samlObject,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val encrypter = buildEncrypterForSamlObject(samlObject, service, adaptor);
        if (encrypter != null) {
            return encrypter.encrypt(samlObject);
        }
        val entityId = adaptor.getEntityId();
        if (service.isEncryptionOptional()) {
            LOGGER.debug("Skipping to encrypt assertion; No encrypter can be determined and encryption is optional for [{}]", entityId);
            return null;
        }
        throw new SamlException("Unable to encrypt assertion for " + entityId);
    }

    /**
     * Encode encrypted id.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @return the encrypted id
     */
    @SneakyThrows
    public EncryptedID encode(final NameID samlObject,
                              final SamlRegisteredService service,
                              final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val encrypter = buildEncrypterForSamlObject(samlObject, service, adaptor);
        if (encrypter != null) {
            return encrypter.encrypt(samlObject);
        }
        val entityId = adaptor.getEntityId();
        if (service.isEncryptionOptional()) {
            LOGGER.debug("Skipping to encrypt Name ID; No encrypter can be determined and encryption is optional for [{}]", entityId);
            return null;
        }
        throw new SamlException("Unable to encrypt Name ID for " + entityId);
    }

    /**
     * Encode encrypted attribute.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @return the encrypted attribute
     */
    @SneakyThrows
    public EncryptedAttribute encode(final Attribute samlObject,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val encrypter = buildEncrypterForSamlObject(samlObject, service, adaptor);
        if (encrypter != null) {
            return encrypter.encrypt(samlObject);
        }
        val entityId = adaptor.getEntityId();
        if (service.isEncryptionOptional()) {
            LOGGER.debug("Skipping to encrypt attribute; No encrypter can be determined and encryption is optional for [{}]", entityId);
            return null;
        }
        throw new SamlException("Unable to encrypt attribute for " + entityId);
    }

    /**
     * Build encrypter for saml object encrypter.
     *
     * @param samlObject the saml object
     * @param service    the service
     * @param adaptor    the adaptor
     * @return the encrypter
     */
    @SneakyThrows
    protected Encrypter buildEncrypterForSamlObject(final Object samlObject,
                                                    final SamlRegisteredService service,
                                                    final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val entityId = adaptor.getEntityId();
        LOGGER.trace("Calculating encryption security configuration for [{}] based on service [{}]", entityId, service.getName());
        val encryptionConfiguration = configureEncryptionSecurityConfiguration(service);

        LOGGER.trace("Fetching key encryption credential for [{}] based on service [{}]", entityId, service.getName());
        configureKeyEncryptionCredential(entityId, adaptor, service, encryptionConfiguration);

        LOGGER.trace("Fetching key encryption parameters for [{}] based on service [{}]", entityId, service.getName());
        val keyEncParams = getKeyEncryptionParameters(samlObject, service, adaptor, encryptionConfiguration);
        if (keyEncParams != null) {
            LOGGER.trace("Key encryption algorithm for [{}] is [{}]", keyEncParams.getRecipient(), keyEncParams.getAlgorithm());
        }

        LOGGER.trace("Fetching data encryption parameters for [{}] based on service [{}]", entityId, service.getName());
        val dataEncParams = getDataEncryptionParameters(samlObject, service, adaptor, encryptionConfiguration);
        if (dataEncParams != null) {
            LOGGER.trace("Data encryption algorithm for [{}] is [{}]", entityId, dataEncParams.getAlgorithm());
        }
        LOGGER.trace("Building encrypter component for [{}]", entityId);
        return getEncrypter(samlObject, service, adaptor, keyEncParams, dataEncParams);
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
    protected Encrypter getEncrypter(final Object samlObject,
                                     final SamlRegisteredService service,
                                     final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                     final KeyEncryptionParameters keyEncParams,
                                     final DataEncryptionParameters dataEncParams) {
        val entityId = adaptor.getEntityId();
        if (keyEncParams == null || dataEncParams == null) {
            LOGGER.warn("No key/data encryption parameters could be determined for [{}]", entityId);
            return null;
        }
        val className = samlObject.getClass().getName();
        val encrypter = new Encrypter(dataEncParams, keyEncParams);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.PEER);
        LOGGER.debug("Attempting to encrypt [{}] for [{}] with key placement of [{}]", className, entityId, encrypter.getKeyPlacement());
        return encrypter;
    }

    /**
     * Gets data encryption parameters.
     *
     * @param samlObject              the saml object
     * @param service                 the service
     * @param adaptor                 the adaptor
     * @param encryptionConfiguration the encryption configuration
     * @return the data encryption parameters
     */
    protected DataEncryptionParameters getDataEncryptionParameters(final Object samlObject,
                                                                   final SamlRegisteredService service,
                                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                   final BasicEncryptionConfiguration encryptionConfiguration) {
        try {
            val params = resolveEncryptionParameters(service, encryptionConfiguration);
            if (params != null) {
                return new DataEncryptionParameters(params);
            }
            LOGGER.debug("No data encryption parameters could be determined");
            return null;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * Gets key encryption parameters.
     *
     * @param samlObject              the saml object
     * @param service                 the service
     * @param adaptor                 the adaptor
     * @param encryptionConfiguration the encryptionConfiguration
     * @return the key encryption parameters
     */
    protected KeyEncryptionParameters getKeyEncryptionParameters(final Object samlObject,
                                                                 final SamlRegisteredService service,
                                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                 final BasicEncryptionConfiguration encryptionConfiguration) {
        try {
            val params = resolveEncryptionParameters(service, encryptionConfiguration);
            if (params != null) {
                return new KeyEncryptionParameters(params, adaptor.getEntityId());
            }
            LOGGER.debug("No key encryption parameters could be determined");
            return null;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets key encryption credential.
     *
     * @param peerEntityId            the peer entity id
     * @param adaptor                 the adaptor
     * @param service                 the service
     * @param encryptionConfiguration the encryption configuration
     * @return the key encryption credential
     * @throws Exception the exception
     */
    protected Credential configureKeyEncryptionCredential(final String peerEntityId,
                                                          final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                          final SamlRegisteredService service,
                                                          final BasicEncryptionConfiguration encryptionConfiguration) throws Exception {

        val kekCredentialResolver = new MetadataCredentialResolver();

        val providers = new ArrayList<KeyInfoProvider>(5);
        providers.add(new RSAKeyValueProvider());
        providers.add(new DSAKeyValueProvider());
        providers.add(new InlineX509DataProvider());
        providers.add(new DEREncodedKeyValueProvider());
        providers.add(new KeyInfoReferenceProvider());

        val keyInfoResolver = new BasicProviderKeyInfoCredentialResolver(providers);
        kekCredentialResolver.setKeyInfoCredentialResolver(keyInfoResolver);

        val roleDescriptorResolver = SamlIdPUtils.getRoleDescriptorResolver(adaptor, samlIdPProperties.getMetadata().isRequireValidMetadata());

        kekCredentialResolver.setRoleDescriptorResolver(roleDescriptorResolver);
        kekCredentialResolver.initialize();

        val criteriaSet = new CriteriaSet();
        criteriaSet.add(new EncryptionConfigurationCriterion(encryptionConfiguration));
        criteriaSet.add(new EntityIdCriterion(peerEntityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new UsageCriterion(UsageType.ENCRYPTION));

        LOGGER.debug("Attempting to resolve the encryption key for entity id [{}]", peerEntityId);
        val credential = kekCredentialResolver.resolveSingle(criteriaSet);

        if (credential == null || credential.getPublicKey() == null) {
            throw new IllegalArgumentException("Unable to resolve the encryption [public] key for entity id " + peerEntityId);
        }

        val encodedKey = EncodingUtils.encodeBase64(credential.getPublicKey().getEncoded());
        LOGGER.debug("Found encryption public key: [{}]", encodedKey);
        encryptionConfiguration.setKeyTransportEncryptionCredentials(CollectionUtils.wrapList(credential));
        return credential;
    }

    /**
     * Resolve encryption parameters.
     *
     * @param service                 the service
     * @param encryptionConfiguration the encryption configuration
     * @return the encryption parameters
     * @throws ResolverException the exception
     */
    protected EncryptionParameters resolveEncryptionParameters(final SamlRegisteredService service,
                                                               final BasicEncryptionConfiguration encryptionConfiguration) throws ResolverException {
        val criteria = new CriteriaSet();
        criteria.add(new EncryptionConfigurationCriterion(encryptionConfiguration));
        criteria.add(new EncryptionOptionalCriterion(service.isEncryptionOptional()));
        return new BasicEncryptionParametersResolver().resolveSingle(criteria);
    }

    /**
     * Configure encryption security configuration.
     *
     * @param service the service
     * @return the basic encryption configuration
     */
    protected BasicEncryptionConfiguration configureEncryptionSecurityConfiguration(final SamlRegisteredService service) {
        val config = DefaultSecurityConfigurationBootstrap.buildDefaultEncryptionConfiguration();
        LOGGER.trace("Default encryption blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.trace("Default encryption key algorithms: [{}]", config.getKeyTransportEncryptionAlgorithms());
        LOGGER.trace("Default encryption data algorithms: [{}]", config.getDataEncryptionAlgorithms());
        LOGGER.trace("Default encryption whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());

        val globalAlgorithms = samlIdPProperties.getAlgs();

        val overrideDataEncryptionAlgorithms = service.getEncryptionDataAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideDataEncryptionAlgorithms()
            : service.getEncryptionDataAlgorithms();
        if (overrideDataEncryptionAlgorithms != null && !overrideDataEncryptionAlgorithms.isEmpty()) {
            config.setDataEncryptionAlgorithms(overrideDataEncryptionAlgorithms);
        }

        val overrideKeyEncryptionAlgorithms = service.getEncryptionKeyAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideKeyEncryptionAlgorithms()
            : service.getEncryptionKeyAlgorithms();
        if (overrideKeyEncryptionAlgorithms != null && !overrideKeyEncryptionAlgorithms.isEmpty()) {
            config.setKeyTransportEncryptionAlgorithms(overrideKeyEncryptionAlgorithms);
        }

        val overrideBlackListedEncryptionAlgorithms = service.getEncryptionBlackListedAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideBlackListedEncryptionAlgorithms()
            : service.getEncryptionBlackListedAlgorithms();
        if (overrideBlackListedEncryptionAlgorithms != null && !overrideBlackListedEncryptionAlgorithms.isEmpty()) {
            config.setBlacklistedAlgorithms(overrideBlackListedEncryptionAlgorithms);
        }

        val overrideWhiteListedAlgorithms = service.getEncryptionWhiteListedAlgorithms().isEmpty()
            ? globalAlgorithms.getOverrideWhiteListedAlgorithms()
            : service.getEncryptionWhiteListedAlgorithms();
        if (overrideWhiteListedAlgorithms != null && !overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(overrideWhiteListedAlgorithms);
        }

        LOGGER.trace("Finalized encryption blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.trace("Finalized encryption key algorithms: [{}]", config.getKeyTransportEncryptionAlgorithms());
        LOGGER.trace("Finalized encryption data algorithms: [{}]", config.getDataEncryptionAlgorithms());
        LOGGER.trace("Finalized encryption whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());

        if (StringUtils.isNotBlank(service.getWhiteListBlackListPrecedence())) {
            val precedence = WhitelistBlacklistConfiguration.Precedence.valueOf(service.getWhiteListBlackListPrecedence().trim().toUpperCase());
            config.setWhitelistBlacklistPrecedence(precedence);
        }
        return config;
    }
}
