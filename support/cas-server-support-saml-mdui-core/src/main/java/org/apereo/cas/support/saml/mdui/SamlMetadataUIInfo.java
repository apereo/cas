package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.LocalizedName;
import org.opensaml.saml.saml2.metadata.LocalizedURI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link SamlMetadataUIInfo}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Setter
@Getter
public class SamlMetadataUIInfo extends DefaultRegisteredServiceUserInterfaceInfo {

    private static final long serialVersionUID = -1434801982864628179L;

    private transient UIInfo uiInfo;

    private String locale;

    /**
     * Instantiates a new Simple metadata uI info.
     *
     * @param registeredService the registered service
     * @param locale            browser preferred language
     */
    public SamlMetadataUIInfo(final RegisteredService registeredService, final String locale) {
        this(null, registeredService);
        this.locale = locale;
    }

    /**
     * Instantiates a new Simple mdui info.
     *
     * @param uiInfo            the ui info
     * @param registeredService the registered service
     */
    public SamlMetadataUIInfo(final UIInfo uiInfo, final RegisteredService registeredService) {
        super(registeredService);
        this.uiInfo = uiInfo;
    }

    /**
     * Gets string values from the list of mdui objects.
     *
     * @param items the items
     * @return the string values
     */
    private static Collection<String> getStringValues(final List<?> items) {
        val list = new ArrayList<String>(items.size());
        items.forEach(d -> {
            if (d instanceof XSURI) {
                list.add(((XSURI) d).getURI());
            } else if (d instanceof XSString) {
                list.add(((XSString) d).getValue());
            }
        });
        return list;
    }

    /**
     * Gets localized values.
     *
     * @param locale browser preferred language
     * @param items  the items
     * @return the string value
     */
    private static String getLocalizedValues(final String locale, final List<?> items) {
        val foundLocale = findLocale(StringUtils.defaultString(locale, "en"), items);
        if (foundLocale.isPresent()) {
            return foundLocale.get();
        }

        if (!items.isEmpty()) {
            val item = items.get(0);
            var value = StringUtils.EMPTY;
            if (item instanceof LocalizedName) {
                value = ((LocalizedName) item).getValue();
            }
            if (item instanceof LocalizedURI) {
                value = ((LocalizedURI) item).getURI();
            }
            if (item instanceof XSString) {
                value = ((XSString) item).getValue();
            }
            LOGGER.trace("Loading first available locale [{}]", value);
            return value;
        }
        return null;
    }

    private static Optional<String> findLocale(final String locale, final List<?> items) {
        LOGGER.trace("Looking for locale [{}]", locale);
        val p = Pattern.compile(locale, Pattern.CASE_INSENSITIVE);
        return items.stream()
            .filter(item -> item instanceof LocalizedName)
            .map(item -> (LocalizedName) item)
            .filter(item -> {
                val xmlLang = item.getXMLLang();
                return StringUtils.isNotBlank(xmlLang) && p.matcher(xmlLang).matches() && StringUtils.isNotBlank(item.getValue());
            })
            .map(XSString::getValue)
            .findFirst();
    }

    /**
     * Gets localized description.
     *
     * @param locale browser preferred language
     * @return the description
     */
    public String getDescription(final String locale) {
        if (this.uiInfo != null) {
            val description = getLocalizedValues(locale, this.uiInfo.getDescriptions());
            return Optional.ofNullable(description).orElseGet(super::getDescription);
        }
        return super.getDescription();
    }

    @Override
    public String getDescription() {
        return getDescription(this.locale);
    }

    @Override
    public String getDisplayName() {
        return getDisplayName(this.locale);
    }

    /**
     * Gets localized displayName.
     *
     * @param locale browser preferred language
     * @return the displayName
     */
    public String getDisplayName(final String locale) {
        if (this.uiInfo != null) {
            val displayName = getLocalizedValues(locale, this.uiInfo.getDisplayNames());
            return Optional.ofNullable(displayName).orElseGet(super::getDisplayName);
        }
        return super.getDisplayName();
    }

    @Override
    public Collection<String> getDescriptions() {
        if (this.uiInfo != null) {
            return getStringValues(this.uiInfo.getDescriptions());
        }
        return super.getDescriptions();
    }

    @Override
    public Collection<String> getDisplayNames() {
        if (this.uiInfo != null) {
            return getStringValues(this.uiInfo.getDisplayNames());
        }
        return super.getDescriptions();
    }

    @Override
    public Collection<String> getInformationURLs() {
        if (this.uiInfo != null) {
            return getStringValues(this.uiInfo.getInformationURLs());
        }
        return super.getInformationURLs();
    }

    @Override
    public String getInformationURL() {
        return getInformationURL(this.locale);
    }

    /**
     * Gets localized informationURL.
     *
     * @param locale browser preferred language
     * @return the informationURL
     */
    public String getInformationURL(final String locale) {
        if (this.uiInfo != null) {
            val informationUrl = getLocalizedValues(locale, this.uiInfo.getInformationURLs());
            return Optional.ofNullable(informationUrl).orElseGet(super::getInformationURL);
        }
        return super.getInformationURL();
    }

    @Override
    public String getPrivacyStatementURL() {
        return getPrivacyStatementURL(this.locale);
    }

    /**
     * Gets localized privacyStatementURL.
     *
     * @param locale browser preferred language
     * @return the privacyStatementURL
     */
    public String getPrivacyStatementURL(final String locale) {
        if (this.uiInfo != null) {
            val privacyStatementURL = getLocalizedValues(locale, this.uiInfo.getPrivacyStatementURLs());
            return Optional.ofNullable(privacyStatementURL).orElseGet(super::getPrivacyStatementURL);
        }
        return super.getPrivacyStatementURL();
    }

    @Override
    public Collection<String> getPrivacyStatementURLs() {
        if (this.uiInfo != null) {
            return getStringValues(this.uiInfo.getPrivacyStatementURLs());
        }
        return super.getPrivacyStatementURLs();
    }

    /**
     * Gets logo urls.
     *
     * @return the logo urls
     */
    @Override
    public Collection<Logo> getLogoUrls() {
        if (this.uiInfo != null) {
            return this.uiInfo.getLogos().stream()
                .map(l -> new Logo(l.getURI(), l.getHeight(), l.getWidth())).collect(Collectors.toList());
        }
        return new ArrayList<>(0);
    }


}
