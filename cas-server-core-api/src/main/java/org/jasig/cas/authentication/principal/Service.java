package org.jasig.cas.authentication.principal;

/**
 * Marker interface for Services. Services are generally either remote
 * applications utilizing CAS or applications that principals wish to gain
 * access to. In most cases this will be some form of web application.
 *
 * @author William G. Thompson, Jr.
 * @author Scott Battaglia
 * @since 3.0.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface Service extends Principal {

    /**
     * Sets the principal.
     *
     * @param principal the new principal
     */
    void setPrincipal(Principal principal);

    /**
     * Whether the services matches another.
     *
     * @param service the service
     * @return true, if successful
     */
    boolean matches(Service service);
}
