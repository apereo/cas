package org.apereo.cas.support.saml.web.consent;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.consent.CasConsentableAttribute;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;

import java.io.Serializable;

/**
 * This is {@link SamlIdPConsentableAttributeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class SamlIdPConsentableAttributeBuilder implements ConsentableAttributeBuilder {
    private final AttributeDefinitionStore attributeDefinitionStore;

    @Override
    public CasConsentableAttribute build(final CasConsentableAttribute attribute) {
        val result = attributeDefinitionStore.locateAttributeDefinition(defn -> {
            if (defn instanceof SamlIdPAttributeDefinition) {
                val samlAttr = (SamlIdPAttributeDefinition) defn;
                return samlAttr.getName().equalsIgnoreCase(attribute.getName())
                       && StringUtils.isNotBlank(samlAttr.getFriendlyName());
            }
            return false;
        });
        if (result.isPresent()) {
            val samlAttr = (SamlIdPAttributeDefinition) result.get();
            attribute.setFriendlyName(samlAttr.getFriendlyName());
        }
        attribute.getValues().replaceAll(o -> {
            if (o instanceof XSString) {
                return ((XSString) o).getValue();
            }
            if (o instanceof XSURI) {
                return ((XSURI) o).getURI();
            }
            if (o instanceof Serializable) {
                return o;
            }
            return o.toString();
        });
        return attribute;
    }
}
