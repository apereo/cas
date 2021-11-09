package org.apereo.cas.configuration.model.support.influxdb;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link InfluxDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-influxdb-core")
@Accessors(chain = true)
public class InfluxDbProperties implements Serializable {

    private static final long serialVersionUID = -1945287308473842616L;

    /**
     * InfluxDb connection url.
     */
    @RequiredProperty
    private String url = "http://localhost:8086";

    /**
     * InfluxDb connection username.
     */
    @RequiredProperty
    private String username = "root";

    /**
     * InfluxDb connection password.
     */
    @RequiredProperty
    private String password = "password";

    /**
     * Database name.
     */
    @RequiredProperty
    private String database;

    /**
     * Organization name.
     */
    @RequiredProperty
    private String organization = "CAS";
}
