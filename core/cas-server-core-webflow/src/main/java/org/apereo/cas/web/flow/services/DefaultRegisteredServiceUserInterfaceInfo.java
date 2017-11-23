package org.apereo.cas.web.flow.services;

import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link DefaultRegisteredServiceUserInterfaceInfo}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultRegisteredServiceUserInterfaceInfo implements Serializable {

    private static final int DEFAULT_IMAGE_SIZE = 32;
    private static final long serialVersionUID = -2416684486715358748L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegisteredServiceUserInterfaceInfo.class);
    
    /** the registered service. **/
    protected final RegisteredService registeredService;

    public DefaultRegisteredServiceUserInterfaceInfo(final RegisteredService registeredService) {
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
     * Gets descriptions.
     *
     * @return the descriptions
     */
    public Collection<String> getDescriptions() {
        return new ArrayList<>(0);
    }

    public Collection<String> getDisplayNames() {
        return new ArrayList<>(0);
    }

    /**
     * Gets information uRL.
     *
     * @return the information uRL
     */
    public String getInformationURL() {
        final Collection<String> items = getInformationURLs();
        if (items.isEmpty()) {
            return this.registeredService.getInformationUrl();
        }
        return StringUtils.collectionToDelimitedString(items, ".");
    }

    /**
     * Gets information uR ls.
     *
     * @return the information uR ls
     */
    public Collection<String> getInformationURLs() {
        return new ArrayList<>(0);
    }


    /**
     * Gets privacy statement uRL.
     *
     * @return the privacy statement uRL
     */
    public String getPrivacyStatementURL() {
        final Collection<String> items = getPrivacyStatementURLs();
        if (items.isEmpty()) {
            return this.registeredService.getPrivacyUrl();
        }
        return StringUtils.collectionToDelimitedString(items, ".");
    }

    /**
     * Gets privacy statement uR ls.
     *
     * @return the privacy statement uR ls
     */
    public Collection<String> getPrivacyStatementURLs() {
        return new ArrayList<>(0);
    }

    /**
     * Gets logo height.
     *
     * @return the logo url
     */
    public long getLogoWidth() {
        try {
            final Collection<Logo> items = getLogoUrls();
            if (!items.isEmpty()) {
                return items.iterator().next().getWidth();
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return DEFAULT_IMAGE_SIZE;
    }

    /**
     * Gets logo height.
     *
     * @return the logo url
     */
    public long getLogoHeight() {
        try {
            final Collection<Logo> items = getLogoUrls();
            if (!items.isEmpty()) {
                return items.iterator().next().getHeight();
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return DEFAULT_IMAGE_SIZE;
    }

    /**
     * Gets logo url.
     *
     * @return the logo url
     */
    public String getLogoUrl() {
        try {
            final Collection<Logo> items = getLogoUrls();
            if (!items.isEmpty()) {
                return items.iterator().next().getUrl();
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
        return new ArrayList<>(0);
    }

    /**
     * The Logo wrapper class for services UI.
     */
    public static class Logo implements Serializable {

        private static final long serialVersionUID = -1434231982864628179L;

        private String url;
        private long height = DEFAULT_IMAGE_SIZE;
        private long width = DEFAULT_IMAGE_SIZE;

        public Logo() {
        }

        public Logo(final String url, final long height, final long width) {
            this.url = url;
            this.height = height;
            this.width = width;
        }

        public long getHeight() {
            return height;
        }

        public void setHeight(final long height) {
            this.height = height;
        }

        public long getWidth() {
            return width;
        }

        public void setWidth(final long width) {
            this.width = width;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(final String url) {
            this.url = url;
        }
    }
}
