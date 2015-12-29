package org.jasig.cas.support.saml.services.idp.metadata.cache;

import com.google.common.cache.CacheLoader;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicResourceCredentialFactoryBean;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.util.ResourceUtils;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.KeyInfoProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DEREncodedKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.AbstractResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ChainingMetadataResolverCacheLoader} that uses Guava's cache loading strategy
 * to keep track of metadata resources and resolvers.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("chainingMetadataResolverCacheLoader")
public final class ChainingMetadataResolverCacheLoader extends CacheLoader<SamlRegisteredService, ChainingMetadataResolver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingMetadataResolverCacheLoader.class);

    private final transient Object lock = new Object();

    @Autowired
    private OpenSamlConfigBean configBean;

    @Value("${cas.samlidp.metadata.failfast.init:true}")
    private boolean failFastInitialization = true;

    @Value("${cas.samlidp.metadata.require.valid:true}")
    private boolean requireValidMetadata = true;

    /**
     * Instantiates a new Chaining metadata resolver cache loader.
     */
    public ChainingMetadataResolverCacheLoader() {
        super();
    }

    @Override
    public ChainingMetadataResolver load(final SamlRegisteredService service) throws Exception {

        final String metadataLocation = service.getMetadataLocation();
        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);

        final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
        try (final InputStream in = metadataResource.getInputStream()) {
            LOGGER.debug("Parsing metadata from [{}]", metadataLocation);
            final Document document = configBean.getParserPool().parse(in);
            final List<MetadataResolver> resolvers = buildSingleMetadataResolver(document, service);
            final ChainingMetadataResolver metadataResolver = new ChainingMetadataResolver();
            synchronized (this.lock) {
                metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
                metadataResolver.setResolvers(resolvers);
                metadataResolver.initialize();
            }
            return metadataResolver;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<MetadataResolver> buildSingleMetadataResolver(final Document document, final SamlRegisteredService service)
            throws Exception {
        try {
            final List<MetadataResolver> resolvers = new ArrayList<>();
            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);

            metadataProvider.setParserPool(configBean.getParserPool());
            metadataProvider.setFailFastInitialization(this.failFastInitialization);
            metadataProvider.setRequireValidMetadata(this.requireValidMetadata);
            metadataProvider.setId(metadataProvider.getClass().getCanonicalName());

            buildMetadataFilters(service, metadataProvider);

            LOGGER.info("Initializing metadata resolver from [{}]", service.getMetadataLocation());
            metadataProvider.initialize();
            LOGGER.info("Initialized metadata resolver from [{}]", service.getMetadataLocation());
            resolvers.add(metadataProvider);
            return resolvers;
        } catch (final ComponentInitializationException ex) {
            throw new RuntimeException("Could not initialize metadata resolver. Resource will be ignored", ex);
        }
    }

    private void buildMetadataFilters(final SamlRegisteredService service, final DOMMetadataResolver metadataProvider) throws Exception {
        final List<MetadataFilter> metadataFilterList = new ArrayList<>();

        if (service.getMetadataMaxValidity() > 0) {
            final RequiredValidUntilFilter requiredValidUntilFilter = new RequiredValidUntilFilter(service.getMetadataMaxValidity());
            metadataFilterList.add(requiredValidUntilFilter);
            LOGGER.debug("Added metadata RequiredValidUntilFilter with max validity of [{}]", service.getMetadataMaxValidity());
        }

        if (StringUtils.isNotBlank(service.getMetadataSignatureLocation())) {
            final AbstractResource resource = ResourceUtils.getResourceFrom(service.getMetadataSignatureLocation());

            final List<KeyInfoProvider> keyInfoProviderList = new ArrayList<>();
            keyInfoProviderList.add(new RSAKeyValueProvider());
            keyInfoProviderList.add(new DSAKeyValueProvider());
            keyInfoProviderList.add(new DEREncodedKeyValueProvider());
            keyInfoProviderList.add(new InlineX509DataProvider());

            final BasicProviderKeyInfoCredentialResolver keyInfoResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviderList);

            final BasicResourceCredentialFactoryBean credentialFactoryBean = new BasicResourceCredentialFactoryBean();
            credentialFactoryBean.setPublicKeyInfo(resource);
            credentialFactoryBean.afterPropertiesSet();
            final BasicCredential credential = credentialFactoryBean.getObject();

            final StaticCredentialResolver resolver = new StaticCredentialResolver(credential);
            final ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(resolver, keyInfoResolver);

            final SignatureValidationFilter signatureValidationFilter = new SignatureValidationFilter(trustEngine);
            signatureValidationFilter.setRequireSignedRoot(false);
            metadataFilterList.add(signatureValidationFilter);
            LOGGER.debug("Added metadata SignatureValidationFilter with signature from [{}]", service.getMetadataSignatureLocation());
        }

        if (!metadataFilterList.isEmpty()) {
            final MetadataFilterChain metadataFilterChain = new MetadataFilterChain();
            metadataFilterChain.setFilters(metadataFilterList);

            LOGGER.debug("Metadata filter chain initialized with [{}] filters", metadataFilterList.size());
            metadataProvider.setMetadataFilter(metadataFilterChain);
        }
    }
}


