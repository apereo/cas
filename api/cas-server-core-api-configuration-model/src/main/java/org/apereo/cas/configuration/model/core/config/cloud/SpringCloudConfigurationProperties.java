package org.apereo.cas.configuration.model.core.config.cloud;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link SpringCloudConfigurationProperties}. This class is only designed here
 * to allow the configuration binding logic to recognize the settings. In actuality, the fields
 * listed here are not used directly as they are directly accessed and fetched via the runtime
 * environment to bootstrap cas settings in form of a property source locator, etc.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
public class SpringCloudConfigurationProperties implements Serializable {
    private static final long serialVersionUID = -2749293768878152908L;

    /**
     * Config config settings.
     */
    private Cloud cloud = new Cloud();

    @Getter
    @Setter
    public static class Cloud implements Serializable {
        /**
         * MongoDb config settings.
         */
        private MongoDb mongo = new MongoDb();
    }

    @RequiresModule(name = "cas-server-support-configuration-cloud-mongo", automated = true)
    @Getter
    @Setter
    public static class MongoDb implements Serializable {
        /**
         * Mongodb URI.
         */
        @RequiredProperty
        private String uri;
    }
}
