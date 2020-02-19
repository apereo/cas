package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.net.URL;

/**
 * Defines the proxying policy for a registered service.
 * While a service may be allowed proxying on a general level,
 * it may still want to restrict who is authorizes to receive
 * the proxy granting ticket. This interface defines the behavior
 * for both options.
 * An additional maximum time to live can be set.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceProxyPolicy extends Serializable {

    /**
     * Determines whether the service is allowed proxy
     * capabilities.
     *
     * @return true, if is allowed to proxy
     */
    boolean isAllowedToProxy();

    /**
     * Determines if the given proxy callback
     * url is authorized and allowed to
     * request proxy access.
     *
     * @param pgtUrl the pgt url
     * @return true, if url allowed.
     */
    boolean isAllowedProxyCallbackUrl(URL pgtUrl);

    /**
     * Maximum time in seconds PGTs would live in CAS server (if defined by a value greater than zero).
     *
     * @return max time to live
     */
    default int getMaxTimeToLiveInSeconds() {
        return 0;
    }
}
