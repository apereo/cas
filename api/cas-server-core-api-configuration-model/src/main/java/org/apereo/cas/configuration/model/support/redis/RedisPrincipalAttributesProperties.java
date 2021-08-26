package org.apereo.cas.configuration.model.support.redis;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link RedisPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-redis-authentication")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("RedisPrincipalAttributesProperties")
public class RedisPrincipalAttributesProperties extends BaseRedisProperties {

    private static final long serialVersionUID = -2373755681488251678L;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;
}
