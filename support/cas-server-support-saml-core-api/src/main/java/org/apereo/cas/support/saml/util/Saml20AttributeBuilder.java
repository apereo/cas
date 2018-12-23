package org.apereo.cas.support.saml.util;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;

/**
 * This is {@link Saml20AttributeBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface Saml20AttributeBuilder {
    /**
     * Build.
     *
     * @param attrStatement the attr statement
     * @param attribute     the attribute
     */
    void build(AttributeStatement attrStatement, Attribute attribute);
}
