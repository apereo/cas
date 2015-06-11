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

package org.jasig.cas.support.saml.web.flow;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.web.support.WebUtils;
import org.joda.time.DateTime;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link SamlMetadataUIParserAction} that attempts to parse
 * the mdui extension block for a SAML SP from the provided metadata locations.
 * The result is put into the flow request context under the parameter
 * {@link #MDUI_FLOW_PARAMETER_NAME}. The entity id parameter is
 * specified by default at {@link #ENTITY_ID_PARAMETER_NAME}.
 *
 * <p>This action is best suited to be invoked when the CAS login page
 * is about to render so that the page, once the MDUI info is obtained,
 * has a chance to populate the UI with relevant info about the SP.</p>
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIParserAction extends AbstractAction {
    /**
     * The default entityId parameter name.
     */
    public static final String ENTITY_ID_PARAMETER_NAME = "entityId";

    /**
     * The default entityId parameter name.
     */
    public static final String MDUI_FLOW_PARAMETER_NAME = "mduiContext";

    private static final int DEFAULT_METADATA_REFRESH_INTERNAL_MINS = 60;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private final String entityIdParameterName;

    private MetadataResolver metadataResolver;

    private boolean requireValidMetadata = true;

    private int refreshIntervalInMinutes = DEFAULT_METADATA_REFRESH_INTERNAL_MINS;

    private DateTime metadataRefreshedDateTime;

    @NotNull
    @Size(min=1)
    private final Collection<Resource> metadataResources;

    @Autowired(required=true)
    @NotNull
    private OpenSamlConfigBean configBean;

    /**
     * Instantiates a new Saml mdui parser action.
     * Defaults the parameter name to {@link #ENTITY_ID_PARAMETER_NAME}.
     *
     * @param metadataResources the metadata resources
     */
    public SamlMetadataUIParserAction(final Collection<Resource> metadataResources) {
        this(ENTITY_ID_PARAMETER_NAME, metadataResources);
    }

    /**
     * Instantiates a new Saml mdui parser action.
     *
     * @param entityIdParameterName the entity id parameter name
     * @param metadataResources     the metadata resources
     */
    public SamlMetadataUIParserAction(final String entityIdParameterName,
                                      final Collection<Resource> metadataResources) {
        this.entityIdParameterName = entityIdParameterName;
        this.metadataResources = metadataResources;
    }

    /**
     * Build metadata resolver from specified resources.
     * Runs the resolution process on the background
     */
    @PostConstruct
    public synchronized void buildMetadataResolver() {
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Resolving metadata from [{}] resources at [{}]", metadataResources.size(),
                        DateTime.now());
                metadataResolver = resolveMetadata(metadataResources);
            }
        });
        t.start();

        try {
            logger.debug("Waiting for metadata resolution to complete...");
            t.join();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("Resolved metadata at [{}].", DateTime.now());
            metadataRefreshedDateTime = DateTime.now();
        }
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final String entityId = request.getParameter(this.entityIdParameterName);
        if (StringUtils.isBlank(entityId)) {
            logger.debug("No entity id found for parameter [{}]", this.entityIdParameterName);
            return new EventFactorySupport().success(this);
        }

        if (shouldRefreshMetadata()) {
            buildMetadataResolver();
        }

        final EntityDescriptor entityDescriptor = getEntityDescriptorForEntityId(entityId);
        if (entityDescriptor == null) {
            logger.debug("Entity descriptor not found for [{}]", entityId);
            return new EventFactorySupport().success(this);
        }

        final SPSSODescriptor spssoDescriptor = getSPSSODescriptor(entityDescriptor);
        if (spssoDescriptor == null) {
            logger.debug("SP SSO descriptor not found for [{}]", entityId);
            return new EventFactorySupport().success(this);
        }

        final Extensions extensions = spssoDescriptor.getExtensions();
        final List<XMLObject> spExtensions = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
        if (spExtensions.isEmpty()) {
            logger.debug("No extensions are found for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return new EventFactorySupport().success(this);
        }

        for (final XMLObject obj : spExtensions) {
            if (obj instanceof UIInfo) {
                final UIInfo uiInfo = (UIInfo) obj;
                logger.debug("Found UI info for [{}]", entityId);
                requestContext.getFlowScope().put(MDUI_FLOW_PARAMETER_NAME, uiInfo);
            }
        }
        return new EventFactorySupport().success(this);
    }

    /**
     * Should refresh metadata based on the interval set?
     *
     * @return true if metadtaa should be refreshed
     */
    private boolean shouldRefreshMetadata() {
        if (this.metadataRefreshedDateTime
                .plusHours(this.refreshIntervalInMinutes)
                .isBeforeNow()) {
            logger.info("Metadata was last refreshed at [{}]. Refreshing...",
                    this.metadataRefreshedDateTime);
            return true;
        }
        return false;
    }

    /**
     * Gets SP SSO descriptor.
     *
     * @param entityDescriptor the entity descriptor
     * @return the sPSSO descriptor
     */
    private SPSSODescriptor getSPSSODescriptor(final EntityDescriptor entityDescriptor) {
        logger.debug("Locating SP SSO descriptor for SAML2 protocol...");
        SPSSODescriptor spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20_NS);
        if (spssoDescriptor == null) {
            logger.debug("Locating SP SSO descriptor for SAML11 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML11P_NS);
        }
        if (spssoDescriptor == null) {
            logger.debug("Locating SP SSO descriptor for SAML1 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML10P_NS);
        }
        logger.debug("SP SSO descriptor resolved to be [{}]", spssoDescriptor);
        return spssoDescriptor;
    }

    /**
     * Gets entity descriptor for entity id.
     *
     * @param entityId the entity id
     * @return the entity descriptor for entity id
     */
    protected EntityDescriptor getEntityDescriptorForEntityId(final String entityId) {
        try {
            final CriteriaSet criterions = new CriteriaSet(new EntityIdCriterion(entityId));
            return this.metadataResolver.resolveSingle(criterions);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Resolve metadata from sources specified, aggregate and return.
     *
     * @param metadataResources the metadata resources
     * @return the metadata resolver
     */
    protected MetadataResolver resolveMetadata(final Collection<Resource> metadataResources) {
        try {

            final ChainingMetadataResolver metadataManager = new ChainingMetadataResolver();
            metadataManager.setId(ChainingMetadataResolver.class.getCanonicalName());

            final List<MetadataResolver> resolvers = new ArrayList<>(metadataResources.size());

            for (final Resource resource : metadataResources) {
                logger.debug("Loading [{}]", resource.getFilename());

                if (!resource.exists() || !resource.isReadable()) {
                    logger.warn("Resource [{}] does not exist or cannot be read", resource.getFilename());
                    continue;
                }

                final Document inCommonMDDoc;
                try (final InputStream in = resource.getInputStream()) {
                    logger.debug("Parsing [{}]", resource.getFilename());
                    inCommonMDDoc = this.configBean.getParserPool().parse(in);
                }

                final Element metadataRoot = inCommonMDDoc.getDocumentElement();
                final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);

                metadataProvider.setParserPool(this.configBean.getParserPool());
                metadataProvider.setFailFastInitialization(true);
                metadataProvider.setRequireValidMetadata(this.requireValidMetadata);
                metadataProvider.setId(metadataProvider.getClass().getCanonicalName());

                logger.debug("Initializing metadata resolver for [{}]", resource.getFilename());
                metadataProvider.initialize();
                resolvers.add(metadataProvider);
            }

            metadataManager.setResolvers(resolvers);

            logger.info("Collected metadata from [{}] resources. Initializing aggregate...", resolvers.size());
            metadataManager.initialize();
            logger.info("Metadata aggregate initialized successfully.", resolvers.size());
            return metadataManager;
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void setRequireValidMetadata(final boolean requireValidMetadata) {
        this.requireValidMetadata = requireValidMetadata;
    }

    public void setRefreshIntervalInMinutes(final int refreshIntervalInMinutes) {
        this.refreshIntervalInMinutes = refreshIntervalInMinutes;
    }
}
