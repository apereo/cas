/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.support.saml.web.flow.mdui;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.filter.impl.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.validation.constraints.NotNull;
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
    /** Logger instance. */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Metadata resources along with filters to perform validation. */
    @NotNull
    protected final Map<Resource, MetadataFilterChain> metadataResources;

    /** Whether the metadata resolver should require valid metadata. Default is true. */
    protected boolean requireValidMetadata = true;

    /** The openSAML config bean. **/
    @Autowired(required=true)
    @NotNull
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
     * @param resource the resource
     * @param entityId the entity id
     * @return the input stream
     * @throws IOException if stream cannot be read
     */
    protected abstract InputStream getResourceInputStream(final Resource resource, final String entityId) throws IOException;

    @Override
    public EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        try {
            final CriteriaSet criterions = new CriteriaSet(new EntityIdCriterion(entityId));
            if (this.metadataResolver != null) {
                return metadataResolver.resolveSingle(criterions);
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return null;

    }

    /**
     * Build metadata resolver aggregate.
     * @param entityId the entity id
     */
    protected final void buildMetadataResolverAggregate(final String entityId) {
        try {
            final List<MetadataResolver> resolvers = new ArrayList<>(metadataResources.size());
            final Set<Map.Entry<Resource, MetadataFilterChain>> entries = metadataResources.entrySet();

            for (final Map.Entry<Resource, MetadataFilterChain> entry : entries) {
                final Resource resource = entry.getKey();

                logger.debug("Loading [{}]", resource.getFilename());
                try (final InputStream in = getResourceInputStream(resource, entityId)) {
                    logger.debug("Parsing [{}]", resource.getFilename());
                    final Document document = this.configBean.getParserPool().parse(in);

                    buildSingleMetadataResolver(resolvers, entry, resource, document);
                    this.metadataResolver = new ChainingMetadataResolver();
                    synchronized (this.lock) {
                        this.metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
                        this.metadataResolver.setResolvers(resolvers);
                        logger.info("Collected metadata from [{}] resources. Initializing aggregate...", resolvers.size());
                        this.metadataResolver.initialize();
                        logger.info("Metadata aggregate initialized successfully.", resolvers.size());
                    }
                } catch (final IOException e) {
                    logger.warn("Could not retrieve input stream from resource. Moving on...");
                    continue;
                }
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Build single metadata resolver.
     *
     * @param resolvers the resolvers
     * @param entry the entry
     * @param resource the resource
     * @param inCommonMDDoc the in common mD doc
     * @throws IOException the iO exception
     */
    private void buildSingleMetadataResolver(final List<MetadataResolver> resolvers,
                                             final Map.Entry<Resource, MetadataFilterChain> entry,
                                             final Resource resource, final Document inCommonMDDoc) throws IOException {
        final Element metadataRoot = inCommonMDDoc.getDocumentElement();
        final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);

        metadataProvider.setParserPool(this.configBean.getParserPool());
        metadataProvider.setFailFastInitialization(true);
        metadataProvider.setRequireValidMetadata(this.requireValidMetadata);
        metadataProvider.setId(metadataProvider.getClass().getCanonicalName());
        if (entry.getValue() != null) {
            metadataProvider.setMetadataFilter(entry.getValue());
        }
        logger.debug("Initializing metadata resolver for [{}]", resource.getURL());

        try {
            metadataProvider.initialize();
            resolvers.add(metadataProvider);
        } catch (final ComponentInitializationException ex) {
            logger.warn("Could not initialize metadata resolver. Resource will be ignored", ex);
        }
    }
}
