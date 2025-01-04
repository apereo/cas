package org.apereo.cas.support.saml.metadata.resolver;

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
    NAME("name"),
    VALUE("value"),
    SIGNATURE("signature");
    private final String columnName;
}
