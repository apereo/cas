package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link SamlRegisteredServiceMetadataColumns}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@RequiredArgsConstructor
public enum SamlRegisteredServiceMetadataColumns {
    /**
     * Name column.
     */
    NAME("name"),
    /**
     * Value column.
     */
    VALUE("value"),
    /**
     * Signature column.
     */
    SIGNATURE("signature");
    
    private final String columnName;
}
