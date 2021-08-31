package org.apereo.cas.metadata;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This is {@link ConfigurationMetadataCatalogQuery}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SuperBuilder
@Getter
@SuppressWarnings("UnnecessaryLambda")
public class ConfigurationMetadataCatalogQuery {
    @Builder.Default
    private final List<String> modules = new ArrayList<>();

    private final QueryTypes queryType;

    @Builder.Default
    private final Predicate<ConfigurationMetadataProperty> queryFilter = casReferenceProperty -> true;

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
}
