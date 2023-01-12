package org.apereo.cas.configuration.model.support.syncope;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link AbstractSyncopeProperties}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AbstractSyncopeProperties")
public abstract class AbstractSyncopeProperties implements Serializable {

    private static final long serialVersionUID = 98513672245088L;

    /**
     * Syncope domain used for authentication, etc.
     * Multiple domains can be separated via comma.
     * Each domain entry results in a separate authentication attempt
     * and transaction by CAS.
     */
    @RequiredProperty
    private String domain = "Master";

    /**
     * Syncope instance URL primary used for REST.
     */
    @RequiredProperty
    private String url;

    /**
     * Map of attributes that optionally may be used to control the names
     * of the collected attributes from Syncope. If an attribute is provided by Syncope,
     * it can be listed here as the key of the map with a value that should be the name
     * of that attribute as collected and recorded by CAS.
     * For example, the convention {@code lastLoginDate->lastDate} will process the
     * Syncope attribute {@code lastLoginDate} and will internally rename that to {@code lastDate}.
     * If no mapping is specified, CAS defaults will be used instead.
     */
    private Map<String, String> attributeMappings = new LinkedHashMap<>();
}
