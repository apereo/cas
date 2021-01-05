package org.apereo.cas.metadata;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ConfigurationMetadataCatalogQuery}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuperBuilder
@Getter
public class ConfigurationMetadataCatalogQuery {
    @Builder.Default
    private final List<String> modules = new ArrayList<>();

    public enum QueryTypes {
        CAS,
        THIRD_PARTY,
        ALL
    }
    private final QueryTypes queryType;
}
