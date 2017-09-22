package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheLoader;
import net.shibboleth.ext.spring.resource.ResourceHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpClientMultithreadedDownloader;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.EntityRoleFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FileBackedHTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.LocalDynamicMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.ResourceBackedMetadataResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
public class ChainingMetadataResolverCacheLoader extends CacheLoader<SamlRegisteredService, ChainingMetadataResolver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingMetadataResolverCacheLoader.class);

    /**
     * The Config bean.
     */
    protected OpenSamlConfigBean configBean;

    /**
     * The Http client.
     */
    protected HttpClient httpClient;

    private final transient Object lock = new Object();

    @Autowired
    private CasConfigurationProperties casProperties;

    public ChainingMetadataResolverCacheLoader(final OpenSamlConfigBean configBean,
                                               final HttpClient httpClient) {
        this.configBean = configBean;
        this.httpClient = httpClient;
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
        LOGGER.info("Loading metadata dynamically for [{}]", service.getName());

        final SamlIdPProperties.Metadata md = casProperties.getAuthn().getSamlIdp().getMetadata();
        final FunctionDrivenDynamicHTTPMetadataResolver resolver =
                new FunctionDrivenDynamicHTTPMetadataResolver(this.httpClient.getWrappedHttpClient());
        resolver.setMinCacheDuration(TimeUnit.MILLISECONDS.convert(md.getCacheExpirationMinutes(), TimeUnit.MINUTES));
        resolver.setRequireValidMetadata(md.isRequireValidMetadata());

        if (StringUtils.isNotBlank(md.getBasicAuthnPassword()) && StringUtils.isNotBlank(md.getBasicAuthnUsername())) {
            resolver.setBasicCredentials(new UsernamePasswordCredentials(md.getBasicAuthnUsername(), md.getBasicAuthnPassword()));
        }
        if (!md.getSupportedContentTypes().isEmpty()) {
            resolver.setSupportedContentTypes(md.getSupportedContentTypes());
        }

        resolver.setRequestURLBuilder(new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final String input) {
                try {
                    if (StringUtils.isNotBlank(input)) {
                        final String metadataLocation = service.getMetadataLocation().replace("{0}", EncodingUtils.urlEncode(input));
                        LOGGER.info("Constructed dynamic metadata query [{}] for [{}]", metadataLocation, service.getName());
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
     * @throws Exception the io exception
     */
    protected void resolveMetadataFromResource(final SamlRegisteredService service,
                                               final List<MetadataResolver> metadataResolvers) throws Exception {

        final String metadataLocation = service.getMetadataLocation();
        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
        final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);

        if (metadataResource instanceof FileSystemResource) {
            resolveFileSystemBasedMetadataResource(service, metadataResolvers, metadataResource);
        }

        if (metadataResource instanceof UrlResource) {
            resolveUrlBasedMetadataResource(service, metadataResolvers, metadataResource);
        }

        if (metadataResource instanceof ClassPathResource) {
            resolveClasspathBasedMetadataResource(service, metadataResolvers, metadataLocation, metadataResource);
        }
    }

    private void resolveClasspathBasedMetadataResource(final SamlRegisteredService service,
                                                       final List<MetadataResolver> metadataResolvers,
                                                       final String metadataLocation,
                                                       final AbstractResource metadataResource) {
        try (InputStream in = metadataResource.getInputStream()) {
            LOGGER.debug("Parsing metadata from [{}]", metadataLocation);
            final Document document = this.configBean.getParserPool().parse(in);

            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);
            buildSingleMetadataResolver(metadataProvider, service);
            metadataResolvers.add(metadataProvider);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void resolveUrlBasedMetadataResource(final SamlRegisteredService service,
                                                 final List<MetadataResolver> metadataResolvers,
                                                 final AbstractResource metadataResource) throws Exception {

        final SamlIdPProperties.Metadata md = casProperties.getAuthn().getSamlIdp().getMetadata();
        final File backupDirectory = new File(md.getLocation().getFile(), "metadata-backups");
        final File backupFile = new File(backupDirectory, metadataResource.getFilename());

        LOGGER.debug("Metadata backup directory is designated to be [{}]", backupDirectory.getCanonicalPath());
        FileUtils.forceMkdir(backupDirectory);

        LOGGER.debug("Metadata backup file will be at [{}]", backupFile.getCanonicalPath());
        FileUtils.forceMkdirParent(backupFile);

        final HttpClientMultithreadedDownloader downloader =
                new HttpClientMultithreadedDownloader(metadataResource, backupFile);

        final FileBackedHTTPMetadataResolver metadataProvider = new FileBackedHTTPMetadataResolver(
                this.httpClient.getWrappedHttpClient(), metadataResource.getURL().toExternalForm(),
                backupFile.getCanonicalPath());
        buildSingleMetadataResolver(metadataProvider, service);
        metadataResolvers.add(metadataProvider);
    }

    private void resolveFileSystemBasedMetadataResource(final SamlRegisteredService service,
                                                        final List<MetadataResolver> metadataResolvers,
                                                        final AbstractResource metadataResource) throws Exception {
        final File metadataFile = metadataResource.getFile();
        final AbstractMetadataResolver metadataResolver;
        if (metadataFile.isDirectory()) {
            metadataResolver = new LocalDynamicMetadataResolver(new FilesystemLoadSaveManager<>(metadataFile, configBean.getParserPool()));
        } else {
            metadataResolver = new ResourceBackedMetadataResolver(ResourceHelper.of(metadataResource));
        }
        buildSingleMetadataResolver(metadataResolver, service);
        metadataResolvers.add(metadataResolver);
    }

    /**
     * Is dynamic metadata query configured ?
     *
     * @param service the service
     * @return true/false
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
        final SamlIdPProperties.Metadata md = casProperties.getAuthn().getSamlIdp().getMetadata();
        metadataProvider.setParserPool(this.configBean.getParserPool());
        metadataProvider.setFailFastInitialization(md.isFailFast());
        metadataProvider.setRequireValidMetadata(md.isRequireValidMetadata());
        metadataProvider.setId(metadataProvider.getClass().getCanonicalName());

        buildMetadataFilters(service, metadataProvider);

        LOGGER.info("Initializing metadata resolver from [{}]", service.getMetadataLocation());
        metadataProvider.initialize();
        LOGGER.info("Initialized metadata resolver from [{}]", service.getMetadataLocation());
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

        buildEntityRoleFilterIfNeeded(service, metadataFilterList);
        buildPredicateFilterIfNeeded(service, metadataFilterList);

        if (!metadataFilterList.isEmpty()) {
            final MetadataFilterChain metadataFilterChain = new MetadataFilterChain();
            metadataFilterChain.setFilters(metadataFilterList);

            LOGGER.debug("Metadata filter chain initialized with [{}] filters", metadataFilterList.size());
            metadataProvider.setMetadataFilter(metadataFilterChain);
        }
    }

    private static void buildEntityRoleFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaRoles())) {
            final List<QName> roles = new ArrayList<>();
            final Set<String> rolesSet = org.springframework.util.StringUtils.commaDelimitedListToSet(service.getMetadataCriteriaRoles());
            rolesSet.stream().forEach(s -> {
                if (s.equalsIgnoreCase(SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    LOGGER.debug("Added entity role filter [{}]", SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
                if (s.equalsIgnoreCase(IDPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    LOGGER.debug("Added entity role filter [{}]", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
            });
            final EntityRoleFilter filter = new EntityRoleFilter(roles);
            filter.setRemoveEmptyEntitiesDescriptors(service.isMetadataCriteriaRemoveEmptyEntitiesDescriptors());
            filter.setRemoveRolelessEntityDescriptors(service.isMetadataCriteriaRemoveRolelessEntityDescriptors());

            metadataFilterList.add(filter);
            LOGGER.debug("Added entity role filter with roles [{}]", roles);
        }
    }


    private static void buildPredicateFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaDirection())
                && StringUtils.isNotBlank(service.getMetadataCriteriaPattern())
                && RegexUtils.isValidRegex(service.getMetadataCriteriaPattern())) {

            final PredicateFilter.Direction dir = PredicateFilter.Direction.valueOf(service.getMetadataCriteriaDirection());
            LOGGER.debug("Metadata predicate filter configuring with direction [{}] and pattern [{}]",
                    service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());

            final PredicateFilter filter = new PredicateFilter(dir, entityDescriptor ->
                    StringUtils.isNotBlank(entityDescriptor.getEntityID())
                            && entityDescriptor.getEntityID().matches(service.getMetadataCriteriaPattern()));

            metadataFilterList.add(filter);
            LOGGER.debug("Added metadata predicate filter with direction [{}] and pattern [{}]",
                    service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());
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
            LOGGER.warn("No metadata signature location is defined for [{}], so SignatureValidationFilter will not be invoked",
                    service.getMetadataLocation());
            return;
        }

        final SignatureValidationFilter signatureValidationFilter =
                SamlUtils.buildSignatureValidationFilter(service.getMetadataSignatureLocation());
        if (signatureValidationFilter != null) {
            signatureValidationFilter.setRequireSignedRoot(false);
            metadataFilterList.add(signatureValidationFilter);
            LOGGER.debug("Added metadata SignatureValidationFilter with signature from [{}]", service.getMetadataSignatureLocation());
        } else {
            LOGGER.warn("Skipped metadata SignatureValidationFilter since signature from [{}] cannot be located",
                    service.getMetadataLocation());
        }
    }


    /**
     * Build required valid until filter if needed. See {@link RequiredValidUntilFilter}.
     *
     * @param service            the service
     * @param metadataFilterList the metadata filter list
     */
    protected void buildRequiredValidUntilFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter>
            metadataFilterList) {
        if (service.getMetadataMaxValidity() > 0) {
            final RequiredValidUntilFilter requiredValidUntilFilter = new RequiredValidUntilFilter(service.getMetadataMaxValidity());
            metadataFilterList.add(requiredValidUntilFilter);
            LOGGER.debug("Added metadata RequiredValidUntilFilter with max validity of [{}]", service.getMetadataMaxValidity());
        } else {
            LOGGER.debug("No metadata maximum validity criteria is defined for [{}], so RequiredValidUntilFilter will not be invoked",
                    service.getMetadataLocation());
        }
    }
}


