package org.apereo.cas.configuration.model.support.syncope;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SyncopeAccountManagementRegistrationProvisioningProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-syncope-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class SyncopeAccountManagementRegistrationProvisioningProperties extends BaseSyncopeProperties {
    @Serial
    private static final long serialVersionUID = 5555936823374022021L;

    /**
     * Syncope realm used for user provisioning.
     * Realms define a hierarchical security domain tree, primarily meant for containing users.
     * The root realm contains everything, and other realms can be seen as containers that split
     * up the total number of entities into smaller pools.
     */
    @RequiredProperty
    private String realm = "/";

    /**
     * Specify the username for REST authentication.
     */
    @RequiredProperty
    private String basicAuthUsername;

    /**
     * Specify the password for REST authentication.
     */
    @RequiredProperty
    private String basicAuthPassword;

    /**
     * Headers, defined as a Map, to include in the request when making the REST call.
     * Will overwrite any header that CAS is pre-defined to
     * send and include in the request. Key in the map should be the header name
     * and the value in the map should be the header value.
     */
    private Map<String, String> headers = new HashMap<>();
}
