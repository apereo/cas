package org.apereo.cas.configuration.model.core.rest;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link RestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-rest")
@Getter
@Setter
@Accessors(chain = true)
public class RestProperties implements Serializable {

    private static final long serialVersionUID = -1833107478273171342L;

    /**
     * Settings related to the REST APIs dealing with registered services.
     */
    @NestedConfigurationProperty
    private RestRegisteredServicesProperties services = new RestRegisteredServicesProperties();

    /**
     * X509 settings related to the rest protocol and authentication.
     */
    @NestedConfigurationProperty
    private RestX509Properties x509 = new RestX509Properties();
}
