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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.web.support.WebUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private final String entityIdParameterName;

    @NotNull
    private final MetadataResolverAdapter metadataAdapter;

    @Autowired
    @NotNull
    private ServicesManager servicesManager;

    /**
     * Instantiates a new SAML mdui parser action.
     * Defaults the parameter name to {@link #ENTITY_ID_PARAMETER_NAME}.
     *
     * @param metadataAdapter the metadata resources
     */
    public SamlMetadataUIParserAction(final MetadataResolverAdapter metadataAdapter) {
        this(ENTITY_ID_PARAMETER_NAME, metadataAdapter);
    }

    /**
     * Instantiates a new SAML mdui parser action.
     *
     * @param entityIdParameterName the entity id parameter name
     * @param metadataAdapter     the metadata adapter
     */
    public SamlMetadataUIParserAction(final String entityIdParameterName,
                                      final MetadataResolverAdapter metadataAdapter) {
        this.entityIdParameterName = entityIdParameterName;
        this.metadataAdapter = metadataAdapter;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final String entityId = request.getParameter(this.entityIdParameterName);
        if (StringUtils.isBlank(entityId)) {
            logger.debug("No entity id found for parameter [{}]", this.entityIdParameterName);
            return success();
        }

        final WebApplicationService service = new SimpleWebApplicationServiceImpl(entityId);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            logger.debug("Entity id [{}] is not recognized/allowed by the CAS service registry", entityId);

            if (registeredService != null) {
                WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext,
                        registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            }

            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Entity " + entityId + " not recognized");
        }

        final EntityDescriptor entityDescriptor = this.metadataAdapter.getEntityDescriptorForEntityId(entityId);
        if (entityDescriptor == null) {
            logger.debug("Entity descriptor not found for [{}]", entityId);
            return success();
        }

        final SPSSODescriptor spssoDescriptor = getSPSSODescriptor(entityDescriptor);
        if (spssoDescriptor == null) {
            logger.debug("SP SSO descriptor not found for [{}]", entityId);
            return success();
        }

        final Extensions extensions = spssoDescriptor.getExtensions();
        if (extensions == null) {
            logger.debug("No extensions are found for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return success();
        }

        final List<XMLObject> spExtensions = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
        if (spExtensions.isEmpty()) {
            logger.debug("No extensions are found for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return success();
        }

        final SimpleMetadataUIInfo mdui = new SimpleMetadataUIInfo(registeredService);

        for (final XMLObject obj : spExtensions) {
            if (obj instanceof UIInfo) {
                final UIInfo uiInfo = (UIInfo) obj;
                logger.debug("Found UI info for [{}] and added to flow context", entityId);
                mdui.setUIInfo(uiInfo);
            }
        }

        requestContext.getFlowScope().put(MDUI_FLOW_PARAMETER_NAME, mdui);
        return success();
    }

    /**
     * Gets SP SSO descriptor.
     *
     * @param entityDescriptor the entity descriptor
     * @return the sPSSO descriptor
     */
    private SPSSODescriptor getSPSSODescriptor(final EntityDescriptor entityDescriptor) {
        logger.debug("Locating SP SSO descriptor for SAML2 protocol...");
        SPSSODescriptor spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
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
}
