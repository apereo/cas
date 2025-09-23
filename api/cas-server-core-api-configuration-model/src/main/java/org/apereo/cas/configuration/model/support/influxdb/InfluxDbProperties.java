package org.apereo.cas.configuration.model.support.influxdb;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -1945287308473842616L;

    /**
     * InfluxDb connection url.
     */
    @RequiredProperty
    private String url = "http://localhost:8181";

    /**
     * Database name.
     */
    @RequiredProperty
    private String database;

    @RequiredProperty
    @ExpressionLanguageCapable
    private String token;

    /**
     * Organization name.
     */
    @RequiredProperty
    private String organization = "CAS";
}
