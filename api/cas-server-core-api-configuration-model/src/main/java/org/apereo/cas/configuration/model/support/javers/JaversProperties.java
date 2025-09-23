package org.apereo.cas.configuration.model.support.javers;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link JaversProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-support-javers")
@Getter
@Setter
@Accessors(chain = true)
public class JaversProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5599875416590735492L;

    /**
     * Family of sub-properties pertaining to MongoDb settings.
     */
    @NestedConfigurationProperty
    private JaversMongoDbProperties mongo = new JaversMongoDbProperties();

}
