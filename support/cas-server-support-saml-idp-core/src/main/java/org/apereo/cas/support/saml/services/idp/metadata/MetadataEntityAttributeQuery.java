package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This is {@link MetadataEntityAttributeQuery}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor(staticName = "of")
@Getter
@ToString
public final class MetadataEntityAttributeQuery {
    private final String name;
    private final String format;
    private final Collection<String> values;
}
