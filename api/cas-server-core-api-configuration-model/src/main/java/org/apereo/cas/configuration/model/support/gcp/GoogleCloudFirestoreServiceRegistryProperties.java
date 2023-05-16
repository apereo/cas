package org.apereo.cas.configuration.model.support.gcp;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link GoogleCloudFirestoreServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-gcp-firestore-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class GoogleCloudFirestoreServiceRegistryProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 5641690796988322918L;

    /**
     * Database collection name to store and fetch registered service definitions.
     */
    @RequiredProperty
    private String collection = "RegisteredServices";

}
