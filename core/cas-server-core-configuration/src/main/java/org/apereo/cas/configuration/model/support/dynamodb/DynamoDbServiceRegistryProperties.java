package org.apereo.cas.configuration.model.support.dynamodb;

import org.apereo.cas.configuration.support.RequiredModule;

/**
 * This is {@link DynamoDbServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredModule(name = "cas-server-support-dynamodb-service-registry")
public class DynamoDbServiceRegistryProperties extends AbstractDynamoDbProperties {
    private static final long serialVersionUID = 809653348774854955L;
}
