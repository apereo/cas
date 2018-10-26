package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.util.EncodingUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
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
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.criterion.EncryptionConfigurationCriterion;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
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
        return encrypter.encrypt(samlObject);
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
        return encrypter.encrypt(samlObject);
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
        return encrypter.encrypt(samlObject);
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
        val className = samlObject.getClass().getName();
        val entityId = adaptor.getEntityId();
        LOGGER.debug("Attempting to encrypt [{}] for [{}]", className, entityId);
        val credential = getKeyEncryptionCredential(entityId, adaptor, service);
        LOGGER.info("Found encryption public key: [{}]", EncodingUtils.encodeBase64(credential.getPublicKey().getEncoded()));

        val keyEncParams = getKeyEncryptionParameters(samlObject, service, adaptor, credential);
        LOGGER.debug("Key encryption algorithm for [{}] is [{}]", keyEncParams.getRecipient(), keyEncParams.getAlgorithm());

        val dataEncParams = getDataEncryptionParameters(samlObject, service, adaptor);
        LOGGER.debug("Data encryption algorithm for [{}] is [{}]", entityId, dataEncParams.getAlgorithm());

        val encrypter = getEncrypter(samlObject, service, adaptor, keyEncParams, dataEncParams);
        LOGGER.debug("Attempting to encrypt [{}] for [{}] with key placement of [{}]",
            className, entityId, encrypter.getKeyPlacement());
        return encrypter;
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
        val encrypter = new Encrypter(dataEncParams, keyEncParams);
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
    protected DataEncryptionParameters getDataEncryptionParameters(final Object samlObject,
                                                                   final SamlRegisteredService service,
                                                                   final SamlRegisteredServiceServiceProviderMetadataFacade adaptor) {
        val dataEncParams = new DataEncryptionParameters();
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
    protected KeyEncryptionParameters getKeyEncryptionParameters(final Object samlObject,
                                                                 final SamlRegisteredService service,
                                                                 final SamlRegisteredServiceServiceProviderMetadataFacade adaptor,
                                                                 final Credential credential) {
        val keyEncParams = new KeyEncryptionParameters();
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
        val config = DefaultSecurityConfigurationBootstrap.buildDefaultEncryptionConfiguration();

        val overrideDataEncryptionAlgorithms = samlIdPProperties.getAlgs().getOverrideDataEncryptionAlgorithms();
        val overrideKeyEncryptionAlgorithms = samlIdPProperties.getAlgs().getOverrideKeyEncryptionAlgorithms();
        val overrideBlackListedEncryptionAlgorithms = samlIdPProperties.getAlgs().getOverrideBlackListedEncryptionAlgorithms();
        val overrideWhiteListedAlgorithms = samlIdPProperties.getAlgs().getOverrideWhiteListedAlgorithms();

        if (overrideBlackListedEncryptionAlgorithms != null && !overrideBlackListedEncryptionAlgorithms.isEmpty()) {
            config.setBlacklistedAlgorithms(overrideBlackListedEncryptionAlgorithms);
        }

        if (overrideWhiteListedAlgorithms != null && !overrideWhiteListedAlgorithms.isEmpty()) {
            config.setWhitelistedAlgorithms(overrideWhiteListedAlgorithms);
        }

        if (overrideDataEncryptionAlgorithms != null && !overrideDataEncryptionAlgorithms.isEmpty()) {
            config.setDataEncryptionAlgorithms(overrideDataEncryptionAlgorithms);
        }

        if (overrideKeyEncryptionAlgorithms != null && !overrideKeyEncryptionAlgorithms.isEmpty()) {
            config.setKeyTransportEncryptionAlgorithms(overrideKeyEncryptionAlgorithms);
        }

        LOGGER.debug("Encryption blacklisted algorithms: [{}]", config.getBlacklistedAlgorithms());
        LOGGER.debug("Encryption key algorithms: [{}]", config.getKeyTransportEncryptionAlgorithms());
        LOGGER.debug("Signature data algorithms: [{}]", config.getDataEncryptionAlgorithms());
        LOGGER.debug("Encryption whitelisted algorithms: [{}]", config.getWhitelistedAlgorithms());

        val kekCredentialResolver = new MetadataCredentialResolver();

        val providers = new ArrayList<KeyInfoProvider>();
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
        criteriaSet.add(new EncryptionConfigurationCriterion(config));
        criteriaSet.add(new EntityIdCriterion(peerEntityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        criteriaSet.add(new UsageCriterion(UsageType.ENCRYPTION));

        LOGGER.debug("Attempting to resolve the encryption key for entity id [{}]", peerEntityId);
        return kekCredentialResolver.resolveSingle(criteriaSet);
    }
}
