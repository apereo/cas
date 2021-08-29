package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link PrincipalAttributesCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PrincipalAttributesCoreProperties")
public class PrincipalAttributesCoreProperties implements Serializable {
    private static final long serialVersionUID = -4525569588579072890L;

    /**
     * Indicates the global cache expiration period, once attributes
     * are fetched from the underlying attribute repository.
     * A zero or negative value indicates that no attribute caching
     * should take place where attributes must always be fetched
     * from the source.
     */
    private int expirationTime = 30;

    /**
     * Expiration caching time unit for attributes.
     */
    private String expirationTimeUnit = TimeUnit.MINUTES.name();

    /**
     * Indicates the global cache size used to store attributes
     * retrieved from the attribute repository.
     */
    private int maximumCacheSize = 10000;

    /**
     * Recover from LDAP exceptions and continue with partial results. Otherwise,
     * die and do not allow to log in.
     */
    private boolean recoverExceptions = true;

    /**
     * Merging strategies can be used to resolve conflicts when the same attribute are found from multiple sources.
     */
    private MergingStrategyTypes merger = MergingStrategyTypes.REPLACE;

    /**
     * Indicates how the results of multiple attribute repositories should
     * be aggregated together.
     */
    private AggregationStrategyTypes aggregation = AggregationStrategyTypes.MERGE;

    /**
     * In the event that multiple attribute repositories are defined,
     * setting this option to {@code true} forces all repositories
     * to produce a person object. If any of the repositories fails to produce
     * a person or person attributes, the resolution engine will halt to
     * short-circuit the process, failing to resolve the person altogether.
     */
    private boolean requireAllRepositorySources;

    /**
     * CAS provides the ability to release a bundle of principal attributes to all services by default.
     * This bundle is not defined on a per-service basis and is always combined with attributes
     * produced by the specific release policy of the service, such that for instance,
     * you can devise rules to always release {@code givenName} and {@code cn} to every application,
     * and additionally allow other specific principal attributes for only some applications
     * per their attribute release policy.
     */
    private Set<String> defaultAttributesToRelease = new HashSet<>(0);

    /**
     * The aggregation strategy types.
     */
    public enum AggregationStrategyTypes {
        /**
         * Default. Designed to query multiple repositories
         * in order and merge the results into a single result set.
         */
        MERGE,
        /**
         *  Query multiple repositories in order and merge the results into
         *  a single result set. As each repository is queried
         *  the attributes from the first query in the result set are
         *  used as the query for the next repository.
         */
        CASCADE
    }

    /**
     * The merging strategy.
     */
    public enum MergingStrategyTypes {
        /**
         * Replace attributes.  Overwrites existing attribute values, if any.
         */
        REPLACE,
        /**
         * Add attributes.
         * Retains existing attribute values if any, and ignores values from subsequent sources in the resolution chain.
         */
        ADD,
        /**
         * No merging.
         * Doesn't merge attributes, ignores attributes from non-authentication attribute repositories.
         */
        NONE,
        /**
         * Multivalued attributes.
         * Combines all values into a single attribute, essentially creating a multi-valued attribute.
         */
        MULTIVALUED;
    }
}
