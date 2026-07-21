package org.apereo.cas.support.saml.services.idp.metadata;

import module java.base;
import org.apereo.cas.support.saml.SamlIdPConstants;
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

    /**
     * Of metadata entity attribute query.
     *
     * @param name   the name
     * @param format the format
     * @param values the values
     * @return the metadata entity attribute query
     */
    public static MetadataEntityAttributeQuery of(final SamlIdPConstants.KnownEntityAttributes name,
                                                  final String format, final Collection<String> values) {
        return of(name.getName(), format, values);
    }

    /**
     * Of metadata entity attribute query.
     *
     * @param name   the name
     * @param format the format
     * @return the metadata entity attribute query
     */
    public static MetadataEntityAttributeQuery of(final SamlIdPConstants.KnownEntityAttributes name,
                                                  final String format) {
        return of(name.getName(), format, List.of());
    }
}
