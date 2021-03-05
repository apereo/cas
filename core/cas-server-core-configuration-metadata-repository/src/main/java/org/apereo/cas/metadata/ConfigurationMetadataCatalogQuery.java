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

    /**
     * The query types.
     */
    public enum QueryTypes {
        /**
         * Fetch settings for CAS.
         */
        CAS,
        /**
         * Fetch settings for all third party libraries.
         */
        THIRD_PARTY,
        /**
         * Fetch settings for all.
         */
        ALL
    }
    
    private final QueryTypes queryType;
}
