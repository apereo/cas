package org.apereo.cas.configuration.model.support.javers;

import org.apereo.cas.configuration.model.support.mongo.BaseMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link JaversMongoDbProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-javers")
@Getter
@Setter
@Accessors(chain = true)
public class JaversMongoDbProperties extends BaseMongoDbProperties {
    @Serial
    private static final long serialVersionUID = -2471243083598934186L;
}
