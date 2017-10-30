package org.apereo.cas.support.saml.mdui;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link AbstractMetadataResolverAdapter} that encapsulates
 * commons between static and dynamic resolvers.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public abstract class AbstractMetadataResolverAdapter implements MetadataResolverAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetadataResolverAdapter.class);

    /**
     * Metadata resources along with filters to perform validation.
     */
    protected Map<Resource, MetadataFilterChain> metadataResources;

    /**
     * Whether the metadata resolver should require valid metadata. Default is true.
     */
    protected boolean requireValidMetadata = true;

    /**
     * The openSAML config bean.
     **/
    protected OpenSamlConfigBean configBean;

    private ChainingMetadataResolver metadataResolver;

    private final Object lock = new Object();

    /**
     * Instantiates a new abstract metadata resolver adapter.
     */
    public AbstractMetadataResolverAdapter() {
        this.metadataResources = new HashMap<>();
    }

    /**
     * Instantiates a new static metadata resolver adapter.
     *
     * @param metadataResources the metadata resources
     */
    public AbstractMetadataResolverAdapter(final Map<Resource, MetadataFilterChain> metadataResources) {
        this.metadataResources = metadataResources;
    }

    public void setRequireValidMetadata(final boolean requireValidMetadata) {
        this.requireValidMetadata = requireValidMetadata;
    }

    /**
     * Retrieve the remote source's input stream to parse data.
     *
     * @param resource the resource
     * @param entityId the entity id
     * @return the input stream
     * @throws IOException if stream cannot be read
     */
    protected InputStream getResourceInputStream(final Resource resource, final String entityId) throws IOException {
        LOGGER.debug("Locating metadata resource from input stream.");
        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("Resource does not exist or is unreadable");
        }
        return resource.getInputStream();
    }

    @Override
    public EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        try {
            final CriteriaSet criterions = new CriteriaSet(new EntityIdCriterion(entityId));
            if (this.metadataResolver != null) {
                return this.metadataResolver.resolveSingle(criterions);
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return null;

    }

    /**
     * Build metadata resolver aggregate.
     */
    public void buildMetadataResolverAggregate() {
        buildMetadataResolverAggregate(null);
    }

    /**
     * Build metadata resolver aggregate. Loops through metadata resources
     * and attempts to resolve the metadata.
     *
     * @param entityId the entity id
     */
    public void buildMetadataResolverAggregate(final String entityId) {
        try {
            LOGGER.debug("Building metadata resolver aggregate");

            this.metadataResolver = new ChainingMetadataResolver();
            final List<MetadataResolver> resolvers = new ArrayList<>();

            final Set<Map.Entry<Resource, MetadataFilterChain>> entries = this.metadataResources.entrySet();
            entries.forEach(entry -> {
                final Resource resource = entry.getKey();
                LOGGER.debug("Loading [{}]", resource.getFilename());
                resolvers.addAll(loadMetadataFromResource(entry.getValue(), resource, entityId));
            });

            synchronized (this.lock) {
                this.metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
                this.metadataResolver.setResolvers(resolvers);
                LOGGER.info("Collected metadata from [{}] resolvers(s). Initializing aggregate resolver...", resolvers.size());
                this.metadataResolver.initialize();
                LOGGER.info("Metadata aggregate initialized successfully.");
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Load metadata from resource.
     *
     * @param metadataFilter the metadata filter
     * @param resource       the resource
     * @param entityId       the entity id
     * @return the list
     */
    private List<MetadataResolver> loadMetadataFromResource(final MetadataFilter metadataFilter,
                                                            final Resource resource, final String entityId) {
        LOGGER.debug("Evaluating metadata resource [{}]", resource.getFilename());
        try (InputStream in = getResourceInputStream(resource, entityId)) {
            if (in.available() > 0 && in.markSupported()) {
                LOGGER.debug("Parsing [{}]", resource.getFilename());
                final Document document = this.configBean.getParserPool().parse(in);
                return buildSingleMetadataResolver(metadataFilter, resource, document);
            }
            LOGGER.warn("Input stream from resource [{}] appears empty. Moving on...", resource.getFilename());
        } catch (final Exception e) {
            LOGGER.warn("Could not retrieve input stream from resource. Moving on...", e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Build single metadata resolver.
     *
     * @param metadataFilterChain the metadata filters chained together
     * @param resource            the resource
     * @param document            the xml document to parse
     * @return list of resolved metadata from resources.
     */
    private List<MetadataResolver> buildSingleMetadataResolver(final MetadataFilter metadataFilterChain,
                                                               final Resource resource, final Document document) {
        try {
            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);

            metadataProvider.setParserPool(this.configBean.getParserPool());
            metadataProvider.setFailFastInitialization(true);
            metadataProvider.setRequireValidMetadata(this.requireValidMetadata);
            metadataProvider.setId(metadataProvider.getClass().getCanonicalName());
            if (metadataFilterChain != null) {
                metadataProvider.setMetadataFilter(metadataFilterChain);
            }
            LOGGER.debug("Initializing metadata resolver for [{}]", resource);
            metadataProvider.initialize();

            final List<MetadataResolver> resolvers = new ArrayList<>();
            resolvers.add(metadataProvider);
            return resolvers;
        } catch (final Exception ex) {
            LOGGER.warn("Could not initialize metadata resolver. Resource will be ignored", ex);
        }
        return new ArrayList<>(0);
    }

    public void setMetadataResources(final Map<Resource, MetadataFilterChain> metadataResources) {
        this.metadataResources = metadataResources;
    }

    public void setConfigBean(final OpenSamlConfigBean configBean) {
        this.configBean = configBean;
    }
}
