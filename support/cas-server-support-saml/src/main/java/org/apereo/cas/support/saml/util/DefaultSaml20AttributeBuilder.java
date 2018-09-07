package org.apereo.cas.support.saml.util;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;

/**
 * This is {@link DefaultSaml20AttributeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class DefaultSaml20AttributeBuilder implements Saml20AttributeBuilder {
    @Override
    public void build(final AttributeStatement attrStatement, final Attribute attribute) {
        attrStatement.getAttributes().add(attribute);
    }
}
