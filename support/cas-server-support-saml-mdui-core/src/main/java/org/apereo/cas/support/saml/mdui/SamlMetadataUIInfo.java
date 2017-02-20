package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.ext.saml2mdui.UIInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link SamlMetadataUIInfo}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIInfo extends DefaultRegisteredServiceUserInterfaceInfo {

    private static final long serialVersionUID = -1434801982864628179L;

    private transient UIInfo uiInfo;

    /**
     * Instantiates a new Simple metadata uI info.
     *
     * @param registeredService the registered service
     */
    public SamlMetadataUIInfo(final RegisteredService registeredService) {
        this(null, registeredService);
    }

    /**
     * Instantiates a new Simple mdui info.
     *
     * @param uiInfo            the ui info
     * @param registeredService the registered service
     */
    public SamlMetadataUIInfo(@Nullable final UIInfo uiInfo, final RegisteredService registeredService) {
        super(registeredService);
        this.uiInfo = uiInfo;
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
        final List<Logo> list = new ArrayList<>();
        if (this.uiInfo != null) {
            list.addAll(this.uiInfo.getLogos().stream().map(l -> new Logo(l.getURL(), l.getHeight(),
                    l.getWidth())).collect(Collectors.toList()));
        }
        return list;
    }

    /**
     * Gets string values from the list of mdui objects.
     *
     * @param items the items
     * @return the string values
     */
    private static Collection<String> getStringValues(final List<?> items) {
        final List<String> list = new ArrayList<>();
        items.forEach(d -> {
            if (d instanceof XSURI) {
                list.add(((XSURI) d).getValue());
            } else if (d instanceof XSString) {
                list.add(((XSString) d).getValue());
            }
        });
        return list;
    }

    public void setUIInfo(final UIInfo uiInfo) {
        this.uiInfo = uiInfo;
    }
}
