package org.apereo.cas.configuration.model.core.multihost;

import org.apereo.cas.configuration.support.RequiredProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A (multi-)host definition.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SimpleHostProperties {

    /**
     * A custom server name.
     */
    @RequiredProperty
    private String serverName;

    /**
     * A custom server prefix.
     */
    @RequiredProperty
    private String serverPrefix;

    /**
     * A custom OIDC issuer.
     */
    @RequiredProperty
    private String oidcIssuer;
}
