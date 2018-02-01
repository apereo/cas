package org.apereo.cas.web.flow.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredService;
import org.springframework.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Getter;

/**
 * This is {@link DefaultRegisteredServiceUserInterfaceInfo}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultRegisteredServiceUserInterfaceInfo implements Serializable {

    private static final int DEFAULT_IMAGE_SIZE = 32;

    private static final long serialVersionUID = -2416684486715358748L;

    /** the registered service. **/
    protected final RegisteredService registeredService;

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
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class Logo implements Serializable {

        private static final long serialVersionUID = -1434231982864628179L;

        private String url;

        private long height = DEFAULT_IMAGE_SIZE;

        private long width = DEFAULT_IMAGE_SIZE;
    }
}
