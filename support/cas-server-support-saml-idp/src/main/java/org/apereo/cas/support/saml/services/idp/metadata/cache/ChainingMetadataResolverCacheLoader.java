package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicResourceCredentialFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.http.HttpClient;
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
import org.opensaml.saml.metadata.resolver.impl.FunctionDrivenDynamicHTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
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
import org.springframework.core.io.AbstractResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.io.IOException;
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
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Config bean.
     */
    protected OpenSamlConfigBean configBean;

    /**
     * The Http client.
     */
    protected HttpClient httpClient;

    private long metadataCacheExpirationMinutes;

    private transient Object lock = new Object();

    private boolean failFastInitialization = true;

    private boolean requireValidMetadata = true;

    private String basicAuthnUsername;
    private String basicAuthnPassword;
    private List<String> supportedContentTypes = new ArrayList<>();

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
        resolver.setRequireValidMetadata(requireValidMetadata);

        if (StringUtils.isNotBlank(this.basicAuthnPassword) && StringUtils.isNotBlank(this.basicAuthnUsername)) {
            resolver.setBasicCredentials(new UsernamePasswordCredentials(this.basicAuthnUsername, this.basicAuthnPassword));
        }
        if (!this.supportedContentTypes.isEmpty()) {
            resolver.setSupportedContentTypes(supportedContentTypes);
        }

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
        try (InputStream in = metadataResource.getInputStream()) {
            logger.debug("Parsing metadata from [{}]", metadataLocation);
            final Document document = this.configBean.getParserPool().parse(in);

            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);
            buildSingleMetadataResolver(metadataProvider, service);
            metadataResolvers.add(metadataProvider);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
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

        buildEntityRoleFilterIfNeeded(service, metadataFilterList);
        buildPredicateFilterIfNeeded(service, metadataFilterList);

        if (!metadataFilterList.isEmpty()) {
            final MetadataFilterChain metadataFilterChain = new MetadataFilterChain();
            metadataFilterChain.setFilters(metadataFilterList);

            logger.debug("Metadata filter chain initialized with [{}] filters", metadataFilterList.size());
            metadataProvider.setMetadataFilter(metadataFilterChain);
        }
    }

    private void buildEntityRoleFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaRoles())) {
            final List<QName> roles = Lists.newArrayList();
            final Set<String> rolesSet = org.springframework.util.StringUtils.commaDelimitedListToSet(service.getMetadataCriteriaRoles());
            rolesSet.stream().forEach(s -> {
                if (s.equalsIgnoreCase(SPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    logger.debug("Added entity role filter [{}]", SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
                if (s.equalsIgnoreCase(IDPSSODescriptor.DEFAULT_ELEMENT_NAME.getLocalPart())) {
                    logger.debug("Added entity role filter [{}]", IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                    roles.add(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                }
            });
            final EntityRoleFilter filter = new EntityRoleFilter(roles);
            filter.setRemoveEmptyEntitiesDescriptors(service.isMetadataCriteriaRemoveEmptyEntitiesDescriptors());
            filter.setRemoveRolelessEntityDescriptors(service.isMetadataCriteriaRemoveRolelessEntityDescriptors());

            metadataFilterList.add(filter);
            logger.debug("Added entity role filter with roles [{}]", roles);
        }
    }


    private void buildPredicateFilterIfNeeded(final SamlRegisteredService service, final List<MetadataFilter> metadataFilterList) {
        if (StringUtils.isNotBlank(service.getMetadataCriteriaDirection())
                && StringUtils.isNotBlank(service.getMetadataCriteriaPattern())
                && RegexUtils.isValidRegex(service.getMetadataCriteriaPattern())) {

            final PredicateFilter.Direction dir = PredicateFilter.Direction.valueOf(service.getMetadataCriteriaDirection());
            logger.debug("Metadata predicate filter configuring with direction [{}] and pattern [{}]",
                    service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern());

            final PredicateFilter filter = new PredicateFilter(dir, entityDescriptor ->
                    StringUtils.isNotBlank(entityDescriptor.getEntityID())
                            && entityDescriptor.getEntityID().matches(service.getMetadataCriteriaPattern()));

            metadataFilterList.add(filter);
            logger.debug("Added metadata predicate filter with direction [{}] and pattern [{}]",
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
            logger.debug("Added metadata RequiredValidUntilFilter with max validity of [{}]", service.getMetadataMaxValidity());
        } else {
            logger.debug("No metadata maximum validity criteria is defined for {}, so RequiredValidUntilFilter will not be invoked",
                    service.getMetadataLocation());
        }
    }

    public void setMetadataCacheExpirationMinutes(final long metadataCacheExpirationMinutes) {
        this.metadataCacheExpirationMinutes = metadataCacheExpirationMinutes;
    }

    public void setFailFastInitialization(final boolean failFastInitialization) {
        this.failFastInitialization = failFastInitialization;
    }

    public void setRequireValidMetadata(final boolean requireValidMetadata) {
        this.requireValidMetadata = requireValidMetadata;
    }

    public void setConfigBean(final OpenSamlConfigBean configBean) {
        this.configBean = configBean;
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setBasicAuthnUsername(final String basicAuthnUsername) {
        this.basicAuthnUsername = basicAuthnUsername;
    }

    public void setBasicAuthnPassword(final String basicAuthnPassword) {
        this.basicAuthnPassword = basicAuthnPassword;
    }

    public void setSupportedContentTypes(final List<String> supportedContentTypes) {
        this.supportedContentTypes = supportedContentTypes;
    }
}


