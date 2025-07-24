package org.apereo.cas.support.saml.web.consent;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.consent.CasConsentableAttribute;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.support.saml.web.idp.profile.builders.attr.SamlIdPAttributeDefinition;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.XSURI;
import java.io.Serializable;
import java.util.ArrayList;

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
            if (defn instanceof final SamlIdPAttributeDefinition samlAttr) {
                return (Strings.CI.equals(samlAttr.getKey(), attribute.getName())
                       || Strings.CI.equals(samlAttr.getUrn(), attribute.getName()))
                          && StringUtils.isNotBlank(samlAttr.getFriendlyName());
            }
            return false;
        });
        if (result.isPresent()) {
            val samlAttr = (SamlIdPAttributeDefinition) result.get();
            attribute.setFriendlyName(samlAttr.getFriendlyName());
        }
        val attributeValues = ObjectUtils.defaultIfNull(attribute.getValues(), new ArrayList<>());
        attributeValues.replaceAll(o -> {
            if (o instanceof final XSString value) {
                return value.getValue();
            }
            if (o instanceof final XSURI value) {
                return value.getURI();
            }
            if (o instanceof Serializable) {
                return o;
            }
            return o.toString();
        });
        return attribute;
    }
}
