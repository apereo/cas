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

import org.jasig.cas.services.RegisteredService;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link SimpleMetadataUIInfo}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class SimpleMetadataUIInfo implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleMetadataUIInfo.class);

    private static final long serialVersionUID = -1434801982864628179L;

    private transient UIInfo uiInfo;

    private final transient RegisteredService registeredService;

    /**
     * Instantiates a new Simple metadata uI info.
     *
     * @param registeredService the registered service
     */
    public SimpleMetadataUIInfo(final RegisteredService registeredService) {
        this(null, registeredService);
    }

    /**
     * Instantiates a new Simple mdui info.
     *
     * @param uiInfo the ui info
     * @param registeredService the registered service
     */
    public SimpleMetadataUIInfo(@Nullable final UIInfo uiInfo, final RegisteredService registeredService) {
        this.uiInfo = uiInfo;
        this.registeredService = registeredService;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        final Collection<String> items = getDescriptions();
        if (items.isEmpty()) {
            return this.registeredService.getDescription();
        }
        return StringUtils.collectionToDelimitedString(items, ".");
    }

    /**
     * Gets descriptions.
     *
     * @return the descriptions
     */
    public Collection<String> getDescriptions() {
        if (uiInfo != null) {
            return getStringValues(uiInfo.getDescriptions());
        }
        return new ArrayList<>();
    }

    /**
     * Gets display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        final Collection<String> items = getDisplayNames();
        if (items.isEmpty()) {
            return this.registeredService.getName();
        }
        return StringUtils.collectionToDelimitedString(items, ".");
    }

    /**
     * Gets display names.
     *
     * @return the display names
     */
    public Collection<String> getDisplayNames() {
        if (uiInfo != null) {
            return getStringValues(uiInfo.getDisplayNames());
        }
        return new ArrayList<>();
    }

    /**
     * Gets information uRL.
     *
     * @return the information uRL
     */
    public String getInformationURL() {
        final Collection<String> items = getInformationURLs();
        return StringUtils.collectionToDelimitedString(items, ".");
    }

    /**
     * Gets information uR ls.
     *
     * @return the information uR ls
     */
    public Collection<String> getInformationURLs() {
        if (uiInfo != null) {
            return getStringValues(uiInfo.getInformationURLs());
        }
        return new ArrayList<>();
    }

    /**
     * Gets privacy statement uRL.
     *
     * @return the privacy statement uRL
     */
    public String getPrivacyStatementURL() {
        final Collection<String> items = getPrivacyStatementURLs();
        return StringUtils.collectionToDelimitedString(items, ".");
    }

    /**
     * Gets privacy statement uR ls.
     *
     * @return the privacy statement uR ls
     */
    public Collection<String> getPrivacyStatementURLs() {
        if (uiInfo != null) {
            return getStringValues(uiInfo.getPrivacyStatementURLs());
        }
        return new ArrayList<>();
    }

    /**
     * Gets logo url.
     *
     * @return the logo url
     */
    public URL getLogoUrl() {
        try {
            final Collection<Logo> items = getLogoUrls();
            if (!items.isEmpty()) {
                return new URL(items.iterator().next().getURL());
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return this.registeredService.getLogo();
    }

    /**
     * Gets logo urls.
     *
     * @return the logo urls
     */
    public Collection<Logo> getLogoUrls() {
        final List<Logo> list = new ArrayList<>();

        if (uiInfo != null) {
            for (final Logo d : uiInfo.getLogos()) {
                list.add(d);
            }
        }

        return list;
    }

    /**
     * Gets string values from the list of mdui objects.
     *
     * @param items the items
     * @return the string values
     */
    private Collection<String> getStringValues(final List<?> items) {
        final List<String> list = new ArrayList<>();
        for (final Object d : items) {
            if (d instanceof XSURI) {
                list.add(((XSURI) d).getValue());
            } else if (d instanceof XSString) {
                list.add(((XSString) d).getValue());
            }
        }
        return list;
    }

    public void setUIInfo(@NotNull final UIInfo uiInfo) {
        this.uiInfo = uiInfo;
    }
}
