package org.apereo.cas.multitenancy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Set;

/**
 * This is {@link TenantAuthenticationProtocolPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TenantAuthenticationProtocolPolicy extends Serializable {

    /**
     * Indicates the collection of CAS protocol versions that this
     * application should allow and support.
     *
     * @return collection of supported protocol versions.
     */
    Set<String> getSupportedProtocols();
}
