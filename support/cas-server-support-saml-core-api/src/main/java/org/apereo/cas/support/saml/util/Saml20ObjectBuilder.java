package org.apereo.cas.support.saml.util;

import org.apereo.cas.support.saml.SamlUtils;

import org.opensaml.saml.common.SAMLObject;

import javax.xml.namespace.QName;

/**
 * This is {@link Saml20ObjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface Saml20ObjectBuilder {

    /**
     * New saml object.
     *
     * @param <T>        the type parameter
     * @param objectType the name id class
     * @return the t
     */
    <T extends SAMLObject> T newSamlObject(Class<T> objectType);

    /**
     * Gets saml object q name.
     *
     * @param objectType the object type
     * @return the saml object q name
     */
    default QName getSamlObjectQName(final Class objectType) {
        return SamlUtils.getSamlObjectQName(objectType);
    }
}
