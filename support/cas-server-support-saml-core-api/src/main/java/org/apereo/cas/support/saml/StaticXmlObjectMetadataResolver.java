package org.apereo.cas.support.saml;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;

/**
 * This is {@link StaticXmlObjectMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class StaticXmlObjectMetadataResolver extends DOMMetadataResolver {

    public StaticXmlObjectMetadataResolver(final XMLObject metadataResource) {
        super(metadataResource.getDOM());
    }
}
