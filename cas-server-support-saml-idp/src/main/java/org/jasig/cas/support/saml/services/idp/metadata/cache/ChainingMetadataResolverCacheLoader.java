package org.jasig.cas.support.saml.services.idp.metadata.cache;

import com.google.common.base.Function;
import com.google.common.cache.CacheLoader;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicResourceCredentialFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.saml.SamlException;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.util.EncodingUtils;
import org.jasig.cas.util.ResourceUtils;
import org.jasig.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.AbstractResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link ChainingMetadataResolverCacheLoader} that uses Guava's cache loading strategy
 * to keep track of metadata resources and resolvers. The cache loader here supports loading
 * metadata resources from SAML services, supports dynamic metadata queries and is able
 * to run various validation filters on the metadata before finally caching the resolver.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("chainingMetadataResolverCacheLoader")
public class ChainingMetadataResolverCacheLoader extends CacheLoader<SamlRegisteredService, ChainingMetadataResolver> {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Config bean.
     */
    @Autowired
    protected OpenSamlConfigBean configBean;

    /**
     * The Http client.
     */
    
    @Autowired
    @Qualifier("noRedirectHttpClient")
    protected HttpClient httpClient;

    @Value("${cas.samlidp.metadata.cache.exp.minutes:30}")
    private long metadataCacheExpirationMinutes;

    private transient Object lock = new Object();

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
        try {
            final ChainingMetadataResolver metadataResolver = new ChainingMetadataResolver();

            final List<MetadataResolver> metadataResolvers = new ArrayList<>();
            if (isDynamicMetadataQueryConfigured(service)) {
                resolveMetadataDynamically(service, metadataResolvers);
            } else {
                resolveMetadataFromResource(service, metadataResolvers);
            }

            if (metadataResolvers.isEmpty()) {
                throw new SamlException("No metadata resolvers could be configured for service " + service.getName()
                        + " with metadata location " + service.getMetadataLocation());
            }

            synchronized (this.lock) {
                metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
                metadataResolver.setResolvers(metadataResolvers);
                metadataResolver.initialize();
            }
            return metadataResolver;
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }

    /**
     * Resolve metadata dynamically.
     *
     * @param service           the service
     * @param metadataResolvers the metadata resolvers
     * @throws Exception the exception
     */
    protected void resolveMetadataDynamically(final SamlRegisteredService service, final List<MetadataResolver> metadataResolvers)
            throws Exception {
        logger.info("Loading metadata dynamically for [{}]", service.getName());
        final FunctionDrivenDynamicHTTPMetadataResolver resolver =
                new FunctionDrivenDynamicHTTPMetadataResolver(this.httpClient.getWrappedHttpClient());
        resolver.setMinCacheDuration(TimeUnit.MILLISECONDS.convert(this.metadataCacheExpirationMinutes, TimeUnit.MINUTES));
        resolver.setRequestURLBuilder(new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final String input) {
                try {
                    if (StringUtils.isNotBlank(input)) {
                        final String metadataLocation = service.getMetadataLocation().replace("{0}", EncodingUtils.urlEncode(input));
                        logger.info("Constructed dynamic metadata query [{}] for [{}]", metadataLocation, service.getName());
                        return metadataLocation;
                    }
                    return null;
                } catch (final Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });
        buildSingleMetadataResolver(resolver, service);
        metadataResolvers.add(resolver);
    }

    /**
     * Resolve metadata from resource.
     *
     * @param service           the service
     * @param metadataResolvers the metadata resolvers
     * @throws IOException the io exception
     */
    protected void resolveMetadataFromResource(final SamlRegisteredService service, final List<MetadataResolver> metadataResolvers)
            throws IOException {
        final String metadataLocation = service.getMetadataLocation();
        logger.info("Loading SAML metadata from [{}]", metadataLocation);
        final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
        try (final InputStream in = metadataResource.getInputStream()) {
            logger.debug("Parsing metadata from [{}]", metadataLocation);
            final Document document = this.configBean.getParserPool().parse(in);

            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);
            buildSingleMetadataResolver(metadataProvider, service);
            metadataResolvers.add(metadataProvider);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Is dynamic metadata query configured boolean.
     *
     * @param service the service
     * @return the boolean
     */
    protected boolean isDynamicMetadataQueryConfigured(final SamlRegisteredService service) {
        return service.getMetadataLocation().trim().endsWith("/entities/{0}");
    }

    /**
     * Build single metadata resolver metadata resolver.
     *
     * @param metadataProvider the metadata provider
     * @param service          the service
     * @throws Exception the exception
     */
    protected void buildSingleMetadataResolver(final AbstractMetadataResolver metadataProvider,
                                               final SamlRegisteredService service) throws Exception {
        metadataProvider.setParserPool(this.configBean.getParserPool());
        metadataProvider.setFailFastInitialization(this.failFastInitialization);
        metadataProvider.setRequireValidMetadata(this.requireValidMetadata);
        metadataProvider.setId(metadataProvider.getClass().getCanonicalName());

        buildMetadataFilters(service, metadataProvider);

        logger.info("Initializing metadata resolver from [{}]", service.getMetadataLocation());
        metadataProvider.initialize();
        logger.info("Initialized metadata resolver from [{}]", service.getMetadataLocation());
    }

    /**
     * Build metadata filters.
     *
     * @param service          the service
     * @param metadataProvider the metadata provider
     * @throws Exception the exception
     */
    protected void buildMetadataFilters(final SamlRegisteredService service, final AbstractMetadataResolver metadataProvider) throws
            Exception {
        final List<MetadataFilter> metadataFilterList = new ArrayList<>();

        buildRequiredValidUntilFilterIfNeeded(service, metadataFilterList);

        buildSignatureValidationFilterIfNeeded(service, metadataFilterList);

        if (!metadataFilterList.isEmpty()) {
            final MetadataFilterChain metadataFilterChain = new MetadataFilterChain();
            metadataFilterChain.setFilters(metadataFilterList);

            logger.debug("Metadata filter chain initialized with [{}] filters", metadataFilterList.size());
            metadataProvider.setMetadataFilter(metadataFilterChain);
        }
    }

    /**
     * Build signature validation filter if needed.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     * @throws Exception the exception
     */
    protected void buildSignatureValidationFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter>
            metadataFilterList) throws Exception {
        if (StringUtils.isBlank(service.getMetadataSignatureLocation())) {
            logger.warn("No metadata signature location is defined for {}, so SignatureValidationFilter will not be invoked",
                    service.getMetadataLocation());
            return;
        }

        final AbstractResource resource = ResourceUtils.getResourceFrom(service.getMetadataSignatureLocation());
        final List<KeyInfoProvider> keyInfoProviderList = new ArrayList<>();
        keyInfoProviderList.add(new RSAKeyValueProvider());
        keyInfoProviderList.add(new DSAKeyValueProvider());
        keyInfoProviderList.add(new DEREncodedKeyValueProvider());
        keyInfoProviderList.add(new InlineX509DataProvider());

        logger.debug("Attempting to resolve credentials from {} for {}",
                service.getMetadataSignatureLocation(), service.getMetadataLocation());

        final BasicProviderKeyInfoCredentialResolver keyInfoResolver = new BasicProviderKeyInfoCredentialResolver(keyInfoProviderList);
        final BasicResourceCredentialFactoryBean credentialFactoryBean = new BasicResourceCredentialFactoryBean();
        credentialFactoryBean.setPublicKeyInfo(resource);
        credentialFactoryBean.afterPropertiesSet();
        final BasicCredential credential = credentialFactoryBean.getObject();

        logger.info("Successfully resolved credentials from {} for {}",
                service.getMetadataSignatureLocation(), service.getMetadataLocation());

        final StaticCredentialResolver resolver = new StaticCredentialResolver(credential);
        final ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(resolver, keyInfoResolver);

        final SignatureValidationFilter signatureValidationFilter = new SignatureValidationFilter(trustEngine);
        signatureValidationFilter.setRequireSignedRoot(false);
        metadataFilterList.add(signatureValidationFilter);
        logger.debug("Added metadata SignatureValidationFilter with signature from [{}]", service.getMetadataSignatureLocation());
    }

    /**
     * Build required valid until filter if needed.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     */
    protected void buildRequiredValidUntilFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter>
            metadataFilterList) {
        if (service.getMetadataMaxValidity() > 0) {
            final RequiredValidUntilFilter requiredValidUntilFilter = new RequiredValidUntilFilter(service.getMetadataMaxValidity());
            metadataFilterList.add(requiredValidUntilFilter);
            logger.debug("Added metadata RequiredValidUntilFilter with max validity of [{}]", service.getMetadataMaxValidity());
        } else {
            logger.debug("No metadata maximum validity criteria is defined for {}, so RequiredValidUntilFilter will not be invoked",
                    service.getMetadataLocation());
        }
    }
}


